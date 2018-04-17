package io.graversen.fiber.event;

import org.ownzone.lib.network.server.management.INetworkClient;

public class ClientDisconnectedEvent implements IEvent
{
    private final INetworkClient networkClient;

    public ClientDisconnectedEvent(INetworkClient networkClient)
    {
        this.networkClient = networkClient;
    }

    public INetworkClient getNetworkClient()
    {
        return networkClient;
    }
}
