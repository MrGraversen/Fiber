package io.graversen.fiber.core.hooks;

import lombok.Value;

import java.util.function.Consumer;

@Value
class EnqueuedNetworkHook<T extends BaseNetworkHook<?>> {
    private final T networkHook;
    private final Consumer<T> dispatcher;

    public void dispatch() {
        dispatcher.accept(networkHook);
    }
}
