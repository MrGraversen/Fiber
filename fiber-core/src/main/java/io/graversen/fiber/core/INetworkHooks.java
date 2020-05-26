package io.graversen.fiber.core;

import io.graversen.fiber.utils.IClient;

public interface INetworkHooks<C extends IClient> {
    void onNetworkRead(C client, NetworkMessage networkMessage);

    void onNetworkWrite(C client, NetworkMessage networkMessage);

    void onClientConnected(C client);

    void onClientDisconnected(C client, Throwable reason);
}
