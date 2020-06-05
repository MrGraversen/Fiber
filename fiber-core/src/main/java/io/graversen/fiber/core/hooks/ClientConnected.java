package io.graversen.fiber.core.hooks;

import io.graversen.fiber.utils.IClient;
import lombok.NonNull;

public class ClientConnected<C extends IClient> extends BaseNetworkHook<C> {
    public ClientConnected(@NonNull C client) {
        super(client);
    }
}
