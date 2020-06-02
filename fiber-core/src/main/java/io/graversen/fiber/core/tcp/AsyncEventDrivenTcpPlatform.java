package io.graversen.fiber.core.tcp;

import io.graversen.fiber.core.IPlatform;
import io.graversen.fiber.core.IServer;
import io.graversen.fiber.core.NoOpServer;
import io.graversen.fiber.core.codec.*;
import io.graversen.fiber.core.hooks.INetworkHooks;
import io.graversen.fiber.core.hooks.NetworkHooksDispatcher;
import io.graversen.fiber.core.tcp.events.*;
import io.graversen.fiber.event.IEventListener;
import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.utils.Checks;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Slf4j
public class AsyncEventDrivenTcpPlatform implements IPlatform<ITcpNetworkClient> {
    private static final int NETWORK_BUFFER_SIZE = 32768;
    private final INetworkHooks<ITcpNetworkClient> networkHooks;
    private final ITcpNetworkClientRepository networkClientRepository;
    private final NetworkQueue networkQueue;
    private final ClientQueues clientQueues;
    private final IEventBus eventBus;
    private AsynchronousTcpServer server;

    public AsyncEventDrivenTcpPlatform(IEventBus eventBus) {
        this.eventBus = Checks.nonNull(eventBus, "eventBus");
        this.networkHooks = new EventDrivenNetworkHooks(eventBus);
        this.networkClientRepository = new DefaultTcpClientRepository();
        this.networkQueue = new NetworkQueue();
        this.clientQueues = new ClientQueues();

        bindDefaultEventHandlers();
    }

    public void start(ServerNetworkConfiguration networkConfiguration) {
        eventBus.start();

        final var networkHooksDispatcher = new NetworkHooksDispatcher(networkHooks);
        server = new AsynchronousTcpServer(
                new AsyncTcpNetworkEngine(networkConfiguration),
                new ServerInternalsConfiguration(NETWORK_BUFFER_SIZE),
                networkClientRepository,
                networkQueue,
                clientQueues,
                new NetworkWriteTask(networkClientRepository, networkQueue, dispatchNetworkPayload()),
                Executors.newCachedThreadPool(),
                new NetworkAcceptHandler(dispatchNetworkClientAccept()),
                new NetworkReadHandler(networkHooksDispatcher, dispatchHandlerFailure()),
                new NetworkWriteHandler(networkHooksDispatcher, clientQueues, networkQueue, dispatchHandlerFailure()),
                networkHooksDispatcher
        );
        server.start(networkHooks);

        log.info("Successfully started {}!", getClass().getSimpleName());
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IServer<ITcpNetworkClient> server() {
        return Objects.requireNonNullElseGet(server, NoOpServer<ITcpNetworkClient>::new);
    }

    @Override
    public <T> void registerDecoder(IDecoder<T> codec, IReceiver<T> receiver) {
        eventBus.registerEventListener(NetworkReadEvent.class, decodeNetworkMessage(codec, receiver));
    }

    @Override
    public <T> EncodeContext<T> registerEncoder(IEncoder<T> encoder) {
        return encodeNetworkMessage(encoder);
    }

    private <T> IEventListener<NetworkReadEvent> decodeNetworkMessage(IDecoder<T> codec, IReceiver<T> receiver) {
        return (NetworkReadEvent event) -> {
            final var decoder = new DecodeContext<>(codec, receiver);
            decoder.decode(event);
        };
    }

    private <T> EncodeContext<T> encodeNetworkMessage(IEncoder<T> encoder) {
        return (value, networkClient) -> {
            final var encodedValue = encoder.encode(value);
            server().send(networkClient, encodedValue);
        };
    }

    private Consumer<NetworkQueuePayload> dispatchNetworkPayload() {
        return payload -> server.doSend(payload);
    }

    private Consumer<ITcpNetworkClient> dispatchNetworkClientAccept() {
        return networkClient -> server.acceptClient(networkClient);
    }

    private BiConsumer<ITcpNetworkClient, Throwable> dispatchHandlerFailure() {
        return (client, reason) -> server.disconnect(client, reason);
    }

    private void bindDefaultEventHandlers() {
        eventBus.registerEventListener(ClientConnectedEvent.class, defaultClientConnectedListener());
        eventBus.registerEventListener(ClientDisconnectedEvent.class, defaultClientDisconnectedListener());
        eventBus.registerEventListener(ServerStartedEvent.class, defaultServerStartedListener());
        eventBus.registerEventListener(ServerStoppedEvent.class, defaultServerStoppedListener());
        eventBus.registerEventListener(NetworkReadEvent.class, defaultNetworkReadListener());
        eventBus.registerEventListener(NetworkWriteEvent.class, defaultNetworkWriteListener());
    }

    private IEventListener<ClientConnectedEvent> defaultClientConnectedListener() {
        return event -> {};
    }

    private IEventListener<ClientDisconnectedEvent> defaultClientDisconnectedListener() {
        return event -> {};
    }

    private IEventListener<ServerStartedEvent> defaultServerStartedListener() {
        return event -> {};
    }

    private IEventListener<ServerStoppedEvent> defaultServerStoppedListener() {
        return event -> {};
    }

    private IEventListener<NetworkReadEvent> defaultNetworkReadListener() {
        return event -> {};
    }

    private IEventListener<NetworkWriteEvent> defaultNetworkWriteListener() {
        return event -> {};
    }
}
