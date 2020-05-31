package io.graversen.fiber.core.hooks;

import io.graversen.fiber.utils.ControllableTaskLoop;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class NetworkHooksDispatcher extends ControllableTaskLoop<EnqueuedNetworkHook<?>> {
    private final BlockingQueue<EnqueuedNetworkHook<?>> networkHookQueue = new LinkedBlockingQueue<>();
    private final INetworkHooks<?> networkHooks;

    public <T extends BaseNetworkHook<?>> void enqueue(T networkHook) {
        final var networkHookConsumer = this.<T>dispatchStrategies().getOrDefault(networkHook.getClass(), failureStrategy());
        final var enqueuedNetworkHook = new EnqueuedNetworkHook<>(networkHook, networkHookConsumer);
        networkHookQueue.offer(enqueuedNetworkHook);
    }

    public void enqueue(NetworkRead<?> networkRead) {
        networkHookQueue.offer(
                new EnqueuedNetworkHook<>(networkRead, hook -> networkHooks.onNetworkRead((NetworkRead) hook))
        );
    }

    public void enqueue(NetworkWrite<?> networkWrite) {
        networkHookQueue.offer(
                new EnqueuedNetworkHook<>(networkWrite, hook -> networkHooks.onNetworkWrite((NetworkWrite) hook))
        );
    }

    public void enqueue(ClientConnected<?> clientConnected) {
        networkHookQueue.offer(
                new EnqueuedNetworkHook<>(clientConnected, hook -> networkHooks.onClientConnected((ClientConnected) hook))
        );
    }

    public void enqueue(ClientDisconnected<?> clientDisconnected) {
        networkHookQueue.offer(
                new EnqueuedNetworkHook<>(clientDisconnected, hook -> networkHooks.onClientDisconnected((ClientDisconnected) hook))
        );
    }

    @Override
    public void performTask(EnqueuedNetworkHook<?> nextItem) {
        nextItem.dispatch();
    }

    @Override
    public EnqueuedNetworkHook<?> awaitNext() throws InterruptedException {
        return networkHookQueue.take();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends BaseNetworkHook<?>> Map<Class<?>, Consumer<T>> dispatchStrategies() {
        return Map.of(
                NetworkRead.class, networkHook -> networkHooks.onNetworkRead((NetworkRead) networkHook),
                NetworkWrite.class, networkHook -> networkHooks.onNetworkWrite((NetworkWrite) networkHook),
                ClientConnected.class, networkHook -> networkHooks.onClientConnected((ClientConnected) networkHook),
                ClientDisconnected.class, networkHook -> networkHooks.onClientDisconnected((ClientDisconnected) networkHook)
        );
    }

    private <T extends BaseNetworkHook<?>> Consumer<T> failureStrategy() {
        return enqueuedNetworkHook -> log.error("Could not enqueue network hook: {}", enqueuedNetworkHook);
    }
}
