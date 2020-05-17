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
import java.util.concurrent.*;

@Slf4j
public abstract class AsynchronousTcpServer implements IServer {
    private final BlockingQueue<NetworkPayload> networkOutQueue;
    private final IEventBus eventBus;
    private final ITcpNetworkClientRepository networkClientRepository;
    private final ServerNetworkConfiguration serverNetworkConfiguration;
    private final ServerInternalsConfiguration serverInternalsConfiguration;
    private final AsynchronousChannelGroup channelGroup;
    private final NetworkWriteTask networkWriteTask;
    private final ExecutorService internalTaskExecutor;

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
        this.serverNetworkConfiguration = serverNetworkConfiguration;
        this.serverInternalsConfiguration = serverInternalsConfiguration;
        this.networkWriteTask = new NetworkWriteTask();
        this.internalTaskExecutor = Executors.newSingleThreadExecutor();

        try {
            this.channelGroup = AsynchronousChannelGroup.withThreadPool(Executors.newSingleThreadExecutor());
        } catch (IOException e) {
            throw new UnableToConfigureServerException(e);
        }

        log.debug("Initialized {} instance", getClass().getSimpleName());
    }

    @Override
    public void start() {
        try {
            final var start = LocalDateTime.now();
            internalTaskExecutor.execute(networkWriteTask);
            serverSocketChannel = AsynchronousServerSocketChannel
                    .open(channelGroup)
                    .bind(serverNetworkConfiguration.getServerAddress());

            serverSocketChannel.accept(ByteBuffer.allocate(serverInternalsConfiguration.getBufferSizeBytes()), networkAcceptHandler());

            final var duration = Duration.between(start, LocalDateTime.now());
            log.debug(
                    "Started {} instance on port {} after {} ms",
                    getClass().getSimpleName(),
                    serverNetworkConfiguration.getBindPort(),
                    duration.toMillis()
            );
        } catch (IOException e) {
            throw new UnableToConfigureServerException(e);
        }
    }

    @Override
    public void stop(Exception reason, boolean gently) {

    }

    @Override
    public void disconnect(IClient client, Exception reason) {

    }

    @Override
    public void broadcast(ByteBuffer message) {
        networkClientRepository.getClients().forEach(networkClient -> send(networkClient, message));
    }

    @Override
    public void send(IClient client, ByteBuffer message) {
        networkOutQueue.offer(new NetworkPayload(message, client));
    }

    void doSend(NetworkPayload networkPayload) {
        networkClientRepository.getClient(networkPayload.getClient()).ifPresent(
                networkClient -> networkClient.socketChannel().write(networkPayload.getByteBuffer())
        );
    }

    CompletionHandler<AsynchronousSocketChannel, ByteBuffer> networkAcceptHandler() {
        return new CompletionHandler<>() {
            @Override
            public void completed(AsynchronousSocketChannel socketChannel, ByteBuffer readBuffer) {
                final var tcpNetworkClient = new TcpNetworkClient(
                        IdUtils.fastClientId(),
                        ClientNetworkDetails.from(socketChannel),
                        LocalDateTime.now(),
                        new ConcurrentHashMap<>(),
                        socketChannel
                );

                log.debug("Registering new client with ID {}", tcpNetworkClient.id());
                networkClientRepository.addClient(tcpNetworkClient);
                socketChannel.read(readBuffer, tcpNetworkClient, networkReadHandler(readBuffer));
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
                    log.debug("Remote disconnect detected from client with ID {}", networkClient.id());
                    networkClientRepository.removeClient(networkClient);
                }
            }

            @Override
            public void failed(Throwable throwable, ITcpNetworkClient networkClient) {
                log.error(throwable.getMessage(), throwable);
            }
        };
    }

    class NetworkWriteTask extends ControllableTaskLoop {
        public NetworkWriteTask() {
            super(Duration.ofSeconds(1));
        }

        @Override
        public void run() {
            selfConfiguration();
            super.run();
        }

        @Override
        public void performTask() {
            try {
                final var nextNetworkPayload = networkOutQueue.take();
                doSend(nextNetworkPayload);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void taskFailed(Exception exception) {
            log.error(exception.getMessage(), exception);
        }

        void selfConfiguration() {
            final var newThreadName = String.format("%s-%d", NetworkWriteTask.class.getSimpleName(), Thread.currentThread().getId());
            Thread.currentThread().setName(newThreadName);
        }
    }
}
