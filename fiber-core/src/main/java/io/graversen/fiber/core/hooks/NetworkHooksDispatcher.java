package io.graversen.fiber.core.hooks;

import io.graversen.fiber.utils.ControllableTaskLoop;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@RequiredArgsConstructor
public class NetworkHooksDispatcher extends ControllableTaskLoop<EnqueuedNetworkHook<?>> {
    private final BlockingQueue<EnqueuedNetworkHook<?>> networkHookQueue = new LinkedBlockingQueue<>();
    private final INetworkHooks<?> networkHooks;

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
}
