package io.graversen.fiber.config.tcp;

public class AllNetworkInterfacesTcpServerConfig extends TcpServerConfig
{
    public AllNetworkInterfacesTcpServerConfig(int bindPort)
    {
        super(bindPort, "0.0.0.0", 1000, 16384, 16384);
    }
}
