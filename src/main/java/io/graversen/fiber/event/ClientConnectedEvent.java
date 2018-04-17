package io.graversen.fiber.event;

import org.ownzone.lib.network.server.management.INetworkClient;

public class ClientConnectedEvent implements IEvent
{
    private final INetworkClient networkClient;

    public ClientConnectedEvent(INetworkClient networkClient)
    {
        this.networkClient = networkClient;
    }

    public INetworkClient getNetworkClient()
    {
        return networkClient;
    }
}
