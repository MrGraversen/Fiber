package io.graversen.fiber.core.hooks;

import io.graversen.fiber.utils.IClient;
import lombok.NonNull;

public class ClientDisconnected<C extends IClient> extends BaseNetworkHook<C> {
    public ClientDisconnected(@NonNull C client) {
        super(client);
    }
}
