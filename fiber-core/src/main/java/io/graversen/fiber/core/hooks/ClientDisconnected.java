package io.graversen.fiber.core.hooks;

import io.graversen.fiber.utils.IClient;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ClientDisconnected<C extends IClient> extends BaseNetworkHook<C> {
    private final Throwable reason;

    public ClientDisconnected(@NonNull C client, Throwable reason) {
        super(client);
        this.reason = reason;
    }
}
