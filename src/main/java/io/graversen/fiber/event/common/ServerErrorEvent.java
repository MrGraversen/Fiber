package io.graversen.fiber.event.common;

import io.graversen.fiber.server.base.AbstractNetworkingServer;

public class ServerErrorEvent extends BaseEvent implements IEvent
{
    private final AbstractNetworkingServer abstractNetworkingServer;
    private final Exception error;

    public ServerErrorEvent(AbstractNetworkingServer abstractNetworkingServer, Exception error)
    {
        this.abstractNetworkingServer = abstractNetworkingServer;
        this.error = error;
    }

    public AbstractNetworkingServer getAbstractNetworkingServer()
    {
        return abstractNetworkingServer;
    }

    public Exception getError()
    {
        return error;
    }

    @Override
    public void print()
    {
        System.out.println(String.format("Event - %s - %s: %s", getClass().getSimpleName(), abstractNetworkingServer.getServerConfig().getServerAddress().toString(), error.getMessage()));
        error.printStackTrace();
    }
}
