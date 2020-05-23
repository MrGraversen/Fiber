package io.graversen.fiber.core;

import io.graversen.fiber.utils.IClient;

public interface IServer {
    void start();

    void stop(Throwable reason);

    void disconnect(IClient client, Throwable reason);

    void broadcast(byte[] message);

    void send(IClient client, byte[] message);
}
