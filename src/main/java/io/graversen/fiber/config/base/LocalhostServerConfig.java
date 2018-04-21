package io.graversen.fiber.config.base;

import io.graversen.fiber.config.base.ServerConfig;

public final class LocalhostServerConfig extends ServerConfig
{
    public LocalhostServerConfig(int bindPort)
    {
        super(bindPort, "127.0.0.1");
    }
}
