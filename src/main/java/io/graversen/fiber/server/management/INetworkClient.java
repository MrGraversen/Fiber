package io.graversen.fiber.server.management;

import io.graversen.fiber.util.NetworkUtil;

public interface INetworkClient
{
    String id();

    String ipAddress();

    int port();

    default String connectionTuple()
    {
        return NetworkUtil.getConnectionTuple(ipAddress(), port());
    }
}
