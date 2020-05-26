package io.graversen.fiber.core.hooks;

import io.graversen.fiber.core.NetworkMessage;
import io.graversen.fiber.utils.IClient;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class NetworkRead<C extends IClient> extends BaseNetworkHook<C> {
    private final @NonNull NetworkMessage networkMessage;

    public NetworkRead(C client, @NonNull NetworkMessage networkMessage) {
        super(client);
        this.networkMessage = networkMessage;
    }
}
