package io.graversen.fiber.event.common;

import io.graversen.fiber.server.base.BaseNetworkingServer;

public class ServerClosedEvent extends BaseEvent implements IEvent
{
    private final BaseNetworkingServer networkingServer;
    private final Exception reason;

    public ServerClosedEvent(BaseNetworkingServer networkingServer, Exception reason)
    {
        this.networkingServer = networkingServer;
        this.reason = reason;
    }

    public BaseNetworkingServer getNetworkingServer()
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
