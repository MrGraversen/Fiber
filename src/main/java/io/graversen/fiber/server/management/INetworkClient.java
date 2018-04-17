package io.graversen.fiber.server.management;

import java.util.UUID;

public interface INetworkClient
{
    UUID id();
    String ipAndPort();
}
