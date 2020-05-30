package io.graversen.fiber.core.tcp;

import io.graversen.fiber.core.IServer;
import io.graversen.fiber.core.NetworkMessage;
import io.graversen.fiber.core.hooks.*;
import io.graversen.fiber.utils.ChannelUtils;
import io.graversen.fiber.utils.Checks;
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

@Slf4j
public abstract class AsynchronousTcpServer implements IServer<ITcpNetworkClient> {
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean stopping = new AtomicBoolean(false);
    private final ServerNetworkConfiguration networkConfiguration;
    private final ServerInternalsConfiguration internalsConfiguration;
    private final ITcpNetworkClientRepository networkClientRepository;
    private final NetworkQueue networkOutQueue;
    private final ClientQueues clientQueues;
    private final NetworkWriteTask networkWriteTask;
    private final ExecutorService internalTaskExecutor;

    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel serverSocketChannel;
    private NetworkHooksDispatcher networkHooksDispatcher;

    public AsynchronousTcpServer(
            ServerNetworkConfiguration networkConfiguration,
            ServerInternalsConfiguration internalsConfiguration,
            ITcpNetworkClientRepository networkClientRepository,
            NetworkQueue networkOutQueue,
            ClientQueues clientQueues,
            NetworkWriteTask networkWriteTask,
            ExecutorService internalTaskExecutor
    ) {
        this.networkConfiguration = Checks.nonNull(networkConfiguration, "networkConfiguration");
        this.internalsConfiguration = Checks.nonNull(internalsConfiguration, "internalsConfiguration");
        this.networkClientRepository = Checks.nonNull(networkClientRepository, "networkClientRepository");
        this.networkOutQueue = Checks.nonNull(networkOutQueue, "networkOutQueue");
        this.clientQueues = Checks.nonNull(clientQueues, "clientQueues");
        this.networkWriteTask = Checks.nonNull(networkWriteTask, "networkWriteTask");
        this.internalTaskExecutor = Checks.nonNull(internalTaskExecutor, "internalTaskExecutor");
        log.debug("Initialized {} instance", getClass().getSimpleName());
    }

    @Override
    public void start(INetworkHooks<ITcpNetworkClient> networkHooks) {
        if (started.compareAndSet(false, true)) {
            Objects.requireNonNull(networkHooks, "Parameter 'networkHooks' must not be null");
            this.networkHooksDispatcher = new NetworkHooksDispatcher(networkHooks);
            internalTaskExecutor.execute(networkHooksDispatcher);

            try {
                final var start = LocalDateTime.now();
                this.channelGroup = AsynchronousChannelGroup.withThreadPool(Executors.newCachedThreadPool());

                internalTaskExecutor.execute(networkWriteTask);
                serverSocketChannel = AsynchronousServerSocketChannel
                        .open(channelGroup)
                        .bind(networkConfiguration.getServerAddress());

                serverSocketChannel.accept(ByteBuffer.allocate(internalsConfiguration.getBufferSizeBytes()), networkAcceptHandler());

                final var duration = Duration.between(start, LocalDateTime.now());
                log.debug(
                        "Started server instance {} on port {} after {} ms",
                        getClass().getSimpleName(),
                        networkConfiguration.getBindPort(),
                        duration.toMillis()
                );
            } catch (IOException e) {
                throw new UnableToConfigureServerException(e);
            }
        } else {
            log.warn("Server instance {} already started!", getClass().getSimpleName());
        }
    }

    @Override
    public void stop(Throwable reason) {
        if (stopping.compareAndSet(false, true)) {
            log.debug("Stopping server with reason: {}", reason.getMessage());
            networkClientRepository.getClients().forEach(networkClient -> disconnect(networkClient, reason));
            channelGroup.shutdown();
            internalTaskExecutor.shutdown();
            ChannelUtils.close(serverSocketChannel);
        } else {
            log.warn("Server instance {} already stopping!", getClass().getSimpleName());
        }
    }

    @Override
    public void disconnect(ITcpNetworkClient client, Throwable reason) {
        try {
            networkClientRepository.getClient(client).ifPresent(networkClient -> {
                log.debug("Client {} disconnected: {}", client.id(), reason.getMessage());
                networkClient.close();
                clientQueues.remove(client);
                networkClientRepository.removeClient(networkClient);
                networkHooksDispatcher.enqueue(new ClientDisconnected<>(client));
            });
        } catch (Exception e) {
            // Nothing useful to do here
        }
    }

    @Override
    public void broadcast(byte[] message) {
        networkClientRepository.getClients().forEach(networkClient -> send(networkClient, message));
    }

    @Override
    public void send(ITcpNetworkClient client, byte[] message) {
        if (!stopping.get()) {
            for (int i = 0; i < message.length; ) {
                final byte[] messagePart = Arrays.copyOfRange(message, i, i + Math.min(message.length, internalsConfiguration.getBufferSizeBytes()));
                networkOutQueue.offer(new NetworkQueuePayload(ByteBuffer.wrap(messagePart), client));
                i += internalsConfiguration.getBufferSizeBytes();
            }
            networkWriteTask.hint();
        } else {
            log.warn("Server instance {} stopping; can't send further messages!", getClass().getSimpleName());
        }
    }

    public void doSend(NetworkQueuePayload payload) {
        final var client = payload.getClient();
        final var socketChannel = client.socketChannel();
        if (socketChannel.isOpen()) {
            try {
                client.pending().set(true);
                final var byteBuffer = payload.getByteBuffer();
                socketChannel.write(byteBuffer, client, networkWriteHandler(byteBuffer));
            } catch (WritePendingException wpe) {
                log.debug("Client {} underlying channel not ready for write; rescheduling on intermediate queue", client.id());
                putOnClientQueue(payload);
            }
        }
    }

    void putOnClientQueue(NetworkQueuePayload networkPayload) {
        networkPayload.registerRequeue();
        final var clientQueue = clientQueues.getClientQueue(networkPayload.getClient());
        clientQueue.offer(networkPayload);
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
                networkHooksDispatcher.enqueue(new ClientConnected<>(networkClient));
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
                    final byte[] message = new byte[readBuffer.remaining()];
                    readBuffer.get(message);

                    networkHooksDispatcher.enqueue(new NetworkRead<>(networkClient, new NetworkMessage(message, message.length)));
                    networkClient.socketChannel().read(readBuffer.clear(), networkClient, this);
                } else if (result == -1) {
                    disconnect(networkClient, new IOException("Disconnect from client endpoint"));
                }
            }

            @Override
            public void failed(Throwable throwable, ITcpNetworkClient networkClient) {
                log.error("Client {} network read error: {}", networkClient.id(), throwable.getMessage());
                disconnect(networkClient, throwable);
            }
        };
    }
}
