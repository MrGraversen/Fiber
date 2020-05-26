package io.graversen.fiber.core.hooks;

import io.graversen.fiber.utils.IClient;

public interface INetworkHooks<C extends IClient> {
    void onNetworkRead(NetworkRead<C> networkRead);

    void onNetworkWrite(NetworkWrite<C> networkWrite);

    void onClientConnected(ClientConnected<C> clientConnected);

    void onClientDisconnected(ClientDisconnected<C> clientDisconnected);
}
