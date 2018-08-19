package io.graversen.fiber.event.common;

import io.graversen.fiber.server.base.BaseNetworkingServer;

public class ServerErrorEvent extends BaseEvent implements IEvent
{
    private final BaseNetworkingServer baseNetworkingServer;
    private final Exception error;

    public ServerErrorEvent(BaseNetworkingServer baseNetworkingServer, Exception error)
    {
        this.baseNetworkingServer = baseNetworkingServer;
        this.error = error;
    }

    public BaseNetworkingServer getBaseNetworkingServer()
    {
        return baseNetworkingServer;
    }

    public Exception getError()
    {
        return error;
    }

    @Override
    public void print()
    {
        System.out.println(String.format("Event - %s - %s: %s", getClass().getSimpleName(), baseNetworkingServer.getServerConfig().getServerAddress().toString(), error.getMessage()));
        error.printStackTrace();
    }
}
