package io.graversen.fiber.config;

import java.net.InetSocketAddress;

public class ServerConfig
{
    private final int bindPort;
    private final String bindAddress;

    public ServerConfig(int bindPort)
    {
        this.bindPort = bindPort;
        this.bindAddress = null;
    }

    public ServerConfig(int bindPort, String bindAddress)
    {
        this.bindPort = bindPort;
        this.bindAddress = bindAddress;
    }

    public int getBindPort()
    {
        return bindPort;
    }

    public String getBindAddress()
    {
        return bindAddress;
    }

    public InetSocketAddress getServerAddress()
    {
        return getBindAddress() == null ? new InetSocketAddress(getBindPort()) : new InetSocketAddress(getBindAddress(), getBindPort());
    }
}
