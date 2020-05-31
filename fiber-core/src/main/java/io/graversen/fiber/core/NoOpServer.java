package io.graversen.fiber.core;

import io.graversen.fiber.core.hooks.INetworkHooks;
import io.graversen.fiber.utils.IClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpServer<C extends IClient> implements IServer<C> {
    @Override
    public void start(INetworkHooks<C> networkHooks) {
        log.warn("Start invoked on " + getClass().getSimpleName());
    }

    @Override
    public void stop(Throwable reason) {
        log.warn("Stop invoked on " + getClass().getSimpleName());
    }

    @Override
    public void disconnect(C client, Throwable reason) {
        log.warn("Disconnect invoked on " + getClass().getSimpleName());
    }

    @Override
    public void broadcast(byte[] message) {
        log.warn("Broadcast invoked on " + getClass().getSimpleName());
    }

    @Override
    public void send(C client, byte[] message) {
        log.warn("Send invoked on " + getClass().getSimpleName());
    }
}
