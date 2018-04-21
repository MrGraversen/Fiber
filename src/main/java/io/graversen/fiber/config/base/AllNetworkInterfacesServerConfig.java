package io.graversen.fiber.config.base;

import io.graversen.fiber.config.base.ServerConfig;

public final class AllNetworkInterfacesServerConfig extends ServerConfig
{
    public AllNetworkInterfacesServerConfig(int bindPort)
    {
        super(bindPort, "0.0.0.0");
    }
}
