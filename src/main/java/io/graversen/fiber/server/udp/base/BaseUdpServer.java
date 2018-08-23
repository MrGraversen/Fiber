package io.graversen.fiber.server.udp.base;

import io.graversen.fiber.config.udp.UdpServerConfig;
import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.server.base.BaseNetworkingServer;
import io.graversen.fiber.server.management.INetworkClient;

public class BaseUdpServer extends BaseNetworkingServer
{
    public BaseUdpServer(UdpServerConfig serverConfig, IEventBus eventBus)
    {
        super(serverConfig, null, eventBus);
        throw new UnsupportedOperationException("UDP server not yet implemented");
    }

    @Override
    public void start()
    {

    }

    @Override
    public void stop(Exception reason, boolean gently)
    {

    }

    @Override
    public void broadcast(byte[] messageData)
    {

    }

    @Override
    public void disconnect(String networkClientId, Exception reason)
    {

    }

    @Override
    public void disconnect(INetworkClient networkClient, Exception reason)
    {

    }

    @Override
    public void send(String networkClientId, byte[] messageData)
    {

    }

    @Override
    public void send(INetworkClient networkClient, byte[] messageData)
    {

    }

    private class UdpSocketServerWrapper
    {

    }
}
