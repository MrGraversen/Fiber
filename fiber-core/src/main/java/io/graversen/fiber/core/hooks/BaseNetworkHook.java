package io.graversen.fiber.core.hooks;

import io.graversen.fiber.utils.IClient;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class BaseNetworkHook<C extends IClient> {
    private final @NonNull C client;
}
