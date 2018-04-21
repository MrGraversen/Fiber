package io.graversen.fiber.config;

public final class LocalhostServerConfig extends ServerConfig
{
    public LocalhostServerConfig(int bindPort)
    {
        super(bindPort, "127.0.0.1");
    }
}
