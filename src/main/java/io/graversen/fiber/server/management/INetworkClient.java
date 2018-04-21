package io.graversen.fiber.server.management;

public interface INetworkClient
{
    String id();

    String ipAddress();

    int port();

    default String connectionTuple()
    {
        return String.format("%s:%d", ipAddress(), port());
    }
}
