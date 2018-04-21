package io.graversen.fiber.event;

import io.graversen.fiber.server.base.AbstractNetworkingServer;

public class ServerClosedEvent extends BaseEvent implements IEvent
{
    private final AbstractNetworkingServer networkingServer;

    public ServerClosedEvent(AbstractNetworkingServer networkingServer)
    {
        this.networkingServer = networkingServer;
    }

    public AbstractNetworkingServer getNetworkingServer()
    {
        return networkingServer;
    }
}
