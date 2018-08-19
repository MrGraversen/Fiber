package io.graversen.fiber.event.common;

import io.graversen.fiber.server.base.BaseNetworkingServer;

public class ServerReadyEvent extends BaseEvent implements IEvent
{
    private final BaseNetworkingServer baseNetworkingServer;

    public ServerReadyEvent(BaseNetworkingServer baseNetworkingServer)
    {
        this.baseNetworkingServer = baseNetworkingServer;
    }

    public BaseNetworkingServer getBaseNetworkingServer()
    {
        return baseNetworkingServer;
    }

    @Override
    public void print()
    {
        System.out.println(String.format("Event - %s - %s", getClass().getSimpleName(), baseNetworkingServer.getServerConfig().getServerAddress().toString()));
    }
}
