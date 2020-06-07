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
import java.util.function.Predicate;

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
        return new EncodeContext<>(encoder, (encodedValue, networkClient) -> server().send(networkClient, encodedValue));
    }

    @Override
    public ClientsContext getClients(Predicate<ITcpNetworkClient> query) {
        final var clients = networkClientRepository.getClients(query);
        return new ClientsContext(server(), clients);
    }

    private <T> IEventListener<NetworkReadEvent> decodeNetworkMessage(IDecoder<T> codec, IReceiver<T> receiver) {
        return (NetworkReadEvent event) -> {
            final var decoder = new DecodeContext<>(codec, receiver);
            decoder.decode(event);
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
}
