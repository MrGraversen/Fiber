package io.graversen.fiber.event;

import io.graversen.fiber.server.base.AbstractNetworkingServer;

public class ServerReadyEvent extends BaseEvent implements IEvent
{
    private final AbstractNetworkingServer abstractNetworkingServer;

    public ServerReadyEvent(AbstractNetworkingServer abstractNetworkingServer)
    {
        this.abstractNetworkingServer = abstractNetworkingServer;
    }

    public AbstractNetworkingServer getAbstractNetworkingServer()
    {
        return abstractNetworkingServer;
    }

    @Override
    public void print()
    {
        System.out.println(String.format("Event - %s - %s", getClass().getSimpleName(), abstractNetworkingServer.getServerConfig().getServerAddress().toString()));
    }
}
