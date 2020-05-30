package io.graversen.fiber.core.tcp;

import io.graversen.fiber.core.IPlatform;
import io.graversen.fiber.core.IServer;
import io.graversen.fiber.core.hooks.INetworkHooks;
import io.graversen.fiber.core.hooks.NetworkHooksDispatcher;
import io.graversen.fiber.event.bus.IEventBus;
import lombok.extern.slf4j.Slf4j;

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
    private AsynchronousTcpServer server;

    public AsyncEventDrivenTcpPlatform(IEventBus eventBus) {
        this.networkHooks = new EventDrivenNetworkHooks(eventBus);
        this.networkClientRepository = new DefaultTcpClientRepository();
        this.networkQueue = new NetworkQueue();
        this.clientQueues = new ClientQueues();
    }

    public void start(ServerNetworkConfiguration networkConfiguration) {
        final var networkHooksDispatcher = new NetworkHooksDispatcher(networkHooks);
        server = new AsynchronousTcpServer(
                networkConfiguration,
                new ServerInternalsConfiguration(NETWORK_BUFFER_SIZE),
                networkClientRepository,
                networkQueue,
                clientQueues,
                new NetworkWriteTask(networkClientRepository, networkQueue, dispatchNetworkPayload()),
                Executors.newCachedThreadPool(),
                new NetworkAcceptHandler(dispatchNetworkClientAccept()),
                new NetworkReadHandler(networkHooksDispatcher, dispatchHandlerFailure()),
                new NetworkWriteHandler(networkHooksDispatcher, clientQueues, networkQueue, dispatchHandlerFailure())
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
        return server;
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
