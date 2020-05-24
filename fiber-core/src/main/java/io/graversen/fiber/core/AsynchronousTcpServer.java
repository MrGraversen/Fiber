package io.graversen.fiber.core;

import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.utils.ChannelUtils;
import io.graversen.fiber.utils.ControllableTaskLoop;
import io.graversen.fiber.utils.IClient;
import io.graversen.fiber.utils.IdUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Slf4j
public class AsynchronousTcpServer implements IServer {
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean stopping = new AtomicBoolean(false);
    private final ServerNetworkConfiguration networkConfiguration;
    private final ServerInternalsConfiguration internalsConfiguration;
    private final IEventBus eventBus;
    private final ITcpNetworkClientRepository networkClientRepository;
    private final NetworkQueue networkOutQueue;
    private final ClientQueues clientQueues;
    private final NetworkWriteTask networkWriteTask;
    private final ExecutorService internalTaskExecutor;

    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel serverSocketChannel;

    public AsynchronousTcpServer(
            ServerNetworkConfiguration networkConfiguration,
            ServerInternalsConfiguration internalsConfiguration,
            IEventBus eventBus,
            ITcpNetworkClientRepository networkClientRepository
    ) {
        this.networkConfiguration = Objects.requireNonNull(networkConfiguration, "Parameter 'networkConfiguration' must not be null");
        this.internalsConfiguration = Objects.requireNonNull(internalsConfiguration, "Parameter 'internalsConfiguration' must not be null");
        this.eventBus = Objects.requireNonNull(eventBus, "Parameter 'eventBus' must not be null");
        this.networkClientRepository = Objects.requireNonNull(networkClientRepository, "Parameter 'eventClass' must not be null");
        this.networkOutQueue = new NetworkQueue();
        this.clientQueues = new ClientQueues();
        this.networkWriteTask = new NetworkWriteTask();
        this.internalTaskExecutor = Executors.newSingleThreadExecutor();
        log.debug("Initialized {} instance", getClass().getSimpleName());
    }

    @Override
    public void start() {
        if (started.compareAndSet(false, true)) {
            try {
                final var start = LocalDateTime.now();

                try {
                    this.channelGroup = AsynchronousChannelGroup.withThreadPool(Executors.newSingleThreadExecutor());
                } catch (IOException e) {
                    throw new UnableToConfigureServerException(e);
                }

                eventBus.start();

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
    }

    // TODO: Wait for messages to be drained
    @Override
    public void stop(Throwable reason) {
        if (stopping.compareAndSet(false, true)) {
            log.debug("Stopping server with reason: {}", reason.getMessage());
            networkClientRepository.getClients().forEach(networkClient -> disconnect(networkClient, reason));
            channelGroup.shutdown();
            internalTaskExecutor.shutdown();
            ChannelUtils.close(serverSocketChannel);
        }
    }

    @Override
    public void disconnect(IClient client, Throwable reason) {
        try {
            networkClientRepository.getClient(client).ifPresent(networkClient -> {
                log.debug("Client {} disconnected: {}", client.id(), reason.getMessage());
                networkClient.close();
                clientQueues.remove(client);
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

    @Override
    public void send(IClient client, byte[] message) {
        if (!stopping.get()) {
            for (int i = 0; i < message.length; ) {
                final byte[] messagePart = Arrays.copyOfRange(message, i, i + internalsConfiguration.getBufferSizeBytes());
                networkOutQueue.offer(new NetworkPayload(ByteBuffer.wrap(messagePart), client));
                i += internalsConfiguration.getBufferSizeBytes();
            }
            networkWriteTask.hint();
        }
    }

    Consumer<ITcpNetworkClient> doSend(NetworkPayload networkPayload) {
        return networkClient -> {
            final var socketChannel = networkClient.socketChannel();
            if (socketChannel.isOpen()) {
                try {
                    networkClient.pending().set(true);
                    socketChannel.write(networkPayload.getByteBuffer(), networkClient, networkWriteHandler());
                } catch (WritePendingException wpe) {
                    log.debug("Underlying channel not ready for write; rescheduling on intermediate queue");
                    putOnClientQueue(networkPayload);
                }
            }
        };
    }

    void putOnClientQueue(NetworkPayload networkPayload) {
        networkPayload.registerRequeue();
        final var clientQueue = clientQueues.getClientQueue(networkPayload.getClient());
        clientQueue.offer(networkPayload);
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

                final var clientQueue = clientQueues.getClientQueue(networkClient);
                final var nextNetworkPayloadOrNull = clientQueue.poll();
                if (nextNetworkPayloadOrNull != null) {
                    networkOutQueue.offer(nextNetworkPayloadOrNull);
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
