package io.graversen.fiber.core.tcp;

import io.graversen.fiber.core.IServer;
import io.graversen.fiber.core.hooks.ClientConnected;
import io.graversen.fiber.core.hooks.ClientDisconnected;
import io.graversen.fiber.core.hooks.INetworkHooks;
import io.graversen.fiber.core.hooks.NetworkHooksDispatcher;
import io.graversen.fiber.utils.ChannelUtils;
import io.graversen.fiber.utils.Checks;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.WritePendingException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class AsynchronousTcpServer implements IServer<ITcpNetworkClient> {
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean stopping = new AtomicBoolean(false);
    private final ServerNetworkConfiguration networkConfiguration;
    private final ServerInternalsConfiguration internalsConfiguration;
    private final ITcpNetworkClientRepository networkClientRepository;
    private final NetworkQueue networkOutQueue;
    private final ClientQueues clientQueues;
    private final NetworkWriteTask networkWriteTask;
    private final ExecutorService internalTaskExecutor;
    private final NetworkAcceptHandler networkAcceptHandler;
    private final NetworkReadHandler networkReadHandler;
    private final NetworkWriteHandler networkWriteHandler;

    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel serverSocketChannel;
    private NetworkHooksDispatcher networkHooksDispatcher;

    public AsynchronousTcpServer(
            ServerNetworkConfiguration networkConfiguration,
            ServerInternalsConfiguration internalsConfiguration,
            ITcpNetworkClientRepository networkClientRepository,
            NetworkQueue networkQueue,
            ClientQueues clientQueues,
            NetworkWriteTask networkWriteTask,
            ExecutorService internalTaskExecutor,
            NetworkAcceptHandler networkAcceptHandler,
            NetworkReadHandler networkReadHandler,
            NetworkWriteHandler networkWriteHandler
    ) {
        this.networkConfiguration = Checks.nonNull(networkConfiguration, "networkConfiguration");
        this.internalsConfiguration = Checks.nonNull(internalsConfiguration, "internalsConfiguration");
        this.networkClientRepository = Checks.nonNull(networkClientRepository, "networkClientRepository");
        this.networkOutQueue = Checks.nonNull(networkQueue, "networkQueue");
        this.clientQueues = Checks.nonNull(clientQueues, "clientQueues");
        this.networkWriteTask = Checks.nonNull(networkWriteTask, "networkWriteTask");
        this.internalTaskExecutor = Checks.nonNull(internalTaskExecutor, "internalTaskExecutor");
        this.networkAcceptHandler = Checks.nonNull(networkAcceptHandler, "networkAcceptHandler");
        this.networkReadHandler = Checks.nonNull(networkReadHandler, "networkReadHandler");
        this.networkWriteHandler = Checks.nonNull(networkWriteHandler, "networkWriteHandler");
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

                serverSocketChannel.accept(null, networkAcceptHandler);

                log.debug(
                        "Started server instance {} on port {} after {} ms",
                        getClass().getSimpleName(),
                        networkConfiguration.getBindPort(),
                        Duration.between(start, LocalDateTime.now()).toMillis()
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
                final int readOffset = i + Math.min(message.length, internalsConfiguration.getBufferSizeBytes());
                final byte[] messagePart = Arrays.copyOfRange(message, i, readOffset);
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
                socketChannel.write(byteBuffer, payload, networkWriteHandler);
            } catch (WritePendingException wpe) {
                log.debug("Client {} underlying channel not ready for write; rescheduling on intermediate queue", client.id());
                putOnClientQueue(payload);
            }
        }
    }

    public void acceptClient(ITcpNetworkClient networkClient) {
        final var readBuffer = ByteBuffer.allocate(internalsConfiguration.getBufferSizeBytes());
        networkClientRepository.addClient(networkClient);
        networkClient.socketChannel().read(readBuffer, new ReadContext(networkClient, readBuffer), networkReadHandler);
        serverSocketChannel.accept(null, networkAcceptHandler);
        networkHooksDispatcher.enqueue(new ClientConnected<>(networkClient));
    }

    void putOnClientQueue(NetworkQueuePayload networkPayload) {
        networkPayload.registerRequeue();
        final var clientQueue = clientQueues.getClientQueue(networkPayload.getClient());
        clientQueue.offer(networkPayload);
    }
}
