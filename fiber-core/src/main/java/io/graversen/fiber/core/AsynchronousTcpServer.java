package io.graversen.fiber.core;

import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.utils.ControllableTaskLoop;
import io.graversen.fiber.utils.IClient;
import io.graversen.fiber.utils.IdUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Slf4j
public abstract class AsynchronousTcpServer implements IServer {
    private final AtomicBoolean stopping = new AtomicBoolean(false);
    private final BlockingQueue<NetworkPayload> networkOutQueue;
    private final IEventBus eventBus;
    private final ITcpNetworkClientRepository networkClientRepository;
    private final ServerNetworkConfiguration networkConfiguration;
    private final ServerInternalsConfiguration internalsConfiguration;
    private final NetworkWriteTask networkWriteTask;
    private final ExecutorService internalTaskExecutor;

    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel serverSocketChannel;

    public AsynchronousTcpServer(
            ServerNetworkConfiguration serverNetworkConfiguration,
            ServerInternalsConfiguration serverInternalsConfiguration,
            IEventBus eventBus,
            ITcpNetworkClientRepository networkClientRepository
    ) {
        this.networkOutQueue = new LinkedBlockingQueue<>();
        this.eventBus = eventBus;
        this.networkClientRepository = networkClientRepository;
        this.networkConfiguration = serverNetworkConfiguration;
        this.internalsConfiguration = serverInternalsConfiguration;
        this.networkWriteTask = new NetworkWriteTask();
        this.internalTaskExecutor = Executors.newSingleThreadExecutor();
        log.debug("Initialized {} instance", getClass().getSimpleName());
    }

    @Override
    public void start() {
        try {
            final var start = LocalDateTime.now();

            try {
                this.channelGroup = AsynchronousChannelGroup.withThreadPool(Executors.newSingleThreadExecutor());
            } catch (IOException e) {
                throw new UnableToConfigureServerException(e);
            }

            internalTaskExecutor.execute(networkWriteTask);
            serverSocketChannel = AsynchronousServerSocketChannel
                    .open(channelGroup)
                    .bind(networkConfiguration.getServerAddress());

            serverSocketChannel.accept(ByteBuffer.allocate(internalsConfiguration.getBufferSizeBytes()), networkAcceptHandler());

            final var duration = Duration.between(start, LocalDateTime.now());
            log.debug(
                    "Started {} instance on port {} after {} ms",
                    getClass().getSimpleName(),
                    networkConfiguration.getBindPort(),
                    duration.toMillis()
            );
        } catch (IOException e) {
            throw new UnableToConfigureServerException(e);
        }
    }

    // TODO: Wait for messages to be drained
    @Override
    public void stop(Throwable reason) {
        log.debug("Stopping server with reason: {}", reason.getMessage());
        stopping.set(true);
        networkClientRepository.getClients().forEach(networkClient -> disconnect(networkClient, reason));
    }

    @Override
    public void disconnect(IClient client, Throwable reason) {
        try {
            networkClientRepository.getClient(client).ifPresent(networkClient -> {
                log.debug("Client {} disconnected: {}", client.id(), reason.getMessage());
                networkClient.close();
                networkClientRepository.removeClient(networkClient);
            });
        } catch (Exception e) {
            // Nothing
        }
    }

    @Override
    public void broadcast(byte[] message) {
        networkClientRepository.getClients().forEach(networkClient -> send(networkClient, message));
    }

    // ByteBuffer must adhere to buffer size
    // Take in byte array, split into buffer sized chunks
    // Add all to queue
    @Override
    public void send(IClient client, byte[] message) {
        if (!stopping.get()) {
            networkOutQueue.offer(new NetworkPayload(ByteBuffer.wrap(message), client));
            networkWriteTask.hint();
        }
    }

    Consumer<ITcpNetworkClient> doSend(NetworkPayload networkPayload) {
        return networkClient -> {
            if (networkClient.socketChannel().isOpen()) {
                final var socketChannel = networkClient.socketChannel();
                final boolean clientReady = networkClient.pending().compareAndSet(false, true);
                final boolean queueExhausted = networkPayload.getRequeueCount().get() >= internalsConfiguration.getMaximumNetworkRequeue();
                if (clientReady || queueExhausted) {
                    if (queueExhausted) {
                        log.debug("Maximum message requeue exhausted, bypassing queue");
                    }

                    socketChannel.write(networkPayload.getByteBuffer(), networkClient, networkWriteHandler());
                } else {
                    log.debug("Client occupied, returning network payload to queue");
                    networkPayload.registerRequeue();
                    networkOutQueue.offer(networkPayload);
                }
            }
        };
    }

    Runnable handleClientUnavailable() {
        return () -> log.debug("Discarding message because client is not available");
    }

    CompletionHandler<AsynchronousSocketChannel, ByteBuffer> networkAcceptHandler() {
        return new CompletionHandler<>() {
            @Override
            public void completed(AsynchronousSocketChannel socketChannel, ByteBuffer readBuffer) {
                final var networkClient = new TcpNetworkClient(
                        IdUtils.fastClientId(),
                        ClientNetworkDetails.from(socketChannel),
                        LocalDateTime.now(),
                        new ConcurrentHashMap<>(),
                        socketChannel
                );

                log.debug("Registering new client with ID {}", networkClient.id());
                networkClientRepository.addClient(networkClient);
                socketChannel.read(readBuffer, networkClient, networkReadHandler(readBuffer));
                serverSocketChannel.accept(readBuffer, this);
            }

            @Override
            public void failed(Throwable throwable, ByteBuffer readBuffer) {
                log.error(throwable.getMessage(), throwable);
            }
        };
    }

    CompletionHandler<Integer, ITcpNetworkClient> networkReadHandler(ByteBuffer readBuffer) {
        return new CompletionHandler<>() {
            @Override
            public void completed(Integer result, ITcpNetworkClient networkClient) {
                if (result > 0) {
                    readBuffer.flip();
                    final byte[] data = new byte[readBuffer.remaining()];
                    readBuffer.get(data);

                    log.debug("Client {}: {}", networkClient.id(), new String(data));
                    networkClient.socketChannel().read(readBuffer.clear(), networkClient, this);
                } else if (result == -1) {
                    disconnect(networkClient, new IOException("Client disconnect"));
                }
            }

            @Override
            public void failed(Throwable throwable, ITcpNetworkClient networkClient) {
                log.error("Client {} network read error: {}", networkClient.id(), throwable.getMessage());
                disconnect(networkClient, throwable);
            }
        };
    }

    CompletionHandler<Integer, ITcpNetworkClient> networkWriteHandler() {
        return new CompletionHandler<>() {
            @Override
            public void completed(Integer result, ITcpNetworkClient networkClient) {
                networkClient.pending().set(false);

                if (result == -1) {
                    disconnect(networkClient, new IOException("Client disconnect"));
                }
            }

            @Override
            public void failed(Throwable throwable, ITcpNetworkClient networkClient) {
                log.error("Client {} network write error: {}", networkClient.id(), throwable.getMessage());
                disconnect(networkClient, throwable);
            }
        };
    }

    class NetworkWriteTask extends ControllableTaskLoop<NetworkPayload> {
        @Override
        public void run() {
            selfConfiguration();
            super.run();
        }

        @Override
        public void performTask(NetworkPayload nextItem) {
            try {
                networkClientRepository.getClient(nextItem.getClient()).ifPresentOrElse(
                        doSend(nextItem),
                        handleClientUnavailable()
                );
            } catch (Exception e) {
                log.error("Client {} unexpected error: {}", nextItem.getClient().id(), e.getMessage());
                disconnect(nextItem.getClient(), e);
            }
        }

        @Override
        public void taskFailed(Exception exception) {
            log.error(exception.getMessage(), exception);
        }

        @Override
        public NetworkPayload awaitNext() throws InterruptedException {
            return networkOutQueue.take();
        }

        void selfConfiguration() {
            final var newThreadName = String.format("%s-%d", NetworkWriteTask.class.getSimpleName(), Thread.currentThread().getId());
            Thread.currentThread().setName(newThreadName);
        }
    }
}
