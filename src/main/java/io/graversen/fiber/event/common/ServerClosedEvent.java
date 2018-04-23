package io.graversen.fiber.event.common;

import io.graversen.fiber.server.base.AbstractNetworkingServer;

public class ServerClosedEvent extends BaseEvent implements IEvent
{
    private final AbstractNetworkingServer networkingServer;
    private final Exception reason;

    public ServerClosedEvent(AbstractNetworkingServer networkingServer, Exception reason)
    {
        this.networkingServer = networkingServer;
        this.reason = reason;
    }

    public AbstractNetworkingServer getNetworkingServer()
    {
        return networkingServer;
    }

    public Exception getReason()
    {
        return reason;
    }

    @Override
    public void print()
    {
        System.out.println(String.format("Event - %s - Reason: %s", getClass().getSimpleName(), reason.getMessage()));
    }
}
