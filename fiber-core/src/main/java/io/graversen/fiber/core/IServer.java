package io.graversen.fiber.core;

import io.graversen.fiber.utils.IClient;

import java.nio.ByteBuffer;

public interface IServer {
    void start();

    void stop(Exception reason, boolean gently);

    void disconnect(IClient client, Exception reason);

    void broadcast(ByteBuffer message);

    void send(IClient client, ByteBuffer message);
}
