package io.graversen.fiber.config;

public final class AllNetworkInterfacesServerConfig extends ServerConfig
{
    public AllNetworkInterfacesServerConfig(int bindPort)
    {
        super(bindPort, "0.0.0.0");
    }
}
