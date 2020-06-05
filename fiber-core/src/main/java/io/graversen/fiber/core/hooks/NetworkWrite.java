package io.graversen.fiber.core.hooks;

import io.graversen.fiber.core.NetworkMessage;
import io.graversen.fiber.utils.IClient;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class NetworkWrite<C extends IClient> extends BaseNetworkHook<C> {
    private final @NonNull NetworkMessage networkMessage;

    public NetworkWrite(C client, @NonNull NetworkMessage networkMessage) {
        super(client);
        this.networkMessage = networkMessage;
    }
}
