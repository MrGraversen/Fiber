package io.graversen.fiber.event;

import io.graversen.fiber.server.management.INetworkClient;

public class ClientDisconnectedEvent extends BaseEvent implements IEvent
{
    private final INetworkClient networkClient;
    private final Exception reason;

    public ClientDisconnectedEvent(INetworkClient networkClient, Exception reason)
    {
        this.networkClient = networkClient;
        this.reason = reason;
    }

    public INetworkClient getNetworkClient()
    {
        return networkClient;
    }

    public Exception getReason()
    {
        return reason;
    }

    @Override
    public void print()
    {
        System.out.println(String.format("Event - %s - %s: %s", getClass().getSimpleName(), networkClient.connectionTuple(), reason.getMessage()));
    }
}
