package io.graversen.fiber.core.hooks;

import io.graversen.fiber.utils.ControllableTaskLoop;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class NetworkHooksDispatcher extends ControllableTaskLoop<EnqueuedNetworkHook<?>> {
    private final BlockingQueue<EnqueuedNetworkHook<?>> networkHookQueue = new LinkedBlockingQueue<>();
    private final INetworkHooks<?> networkHooks;

    public <T extends BaseNetworkHook<?>> void enqueue(T networkHook) {
        final var networkHookConsumer = this.<T>dispatchStrategies().getOrDefault(networkHook.getClass(), failureStrategy());
        final var enqueuedNetworkHook = new EnqueuedNetworkHook<>(networkHook, networkHookConsumer);
        networkHookQueue.offer(enqueuedNetworkHook);
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
        return enqueuedNetworkHook -> {
            throw new IllegalArgumentException("Could not enqueue network hook");
        };
    }
}
