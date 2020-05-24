package io.graversen.fiber.core;

import io.graversen.fiber.utils.IClient;

public interface IServer<C extends IClient> {
    void start(INetworkHooks<C> networkHooks);

    void stop(Throwable reason);

    void disconnect(C client, Throwable reason);

    void broadcast(byte[] message);

    void send(C client, byte[] message);
}
