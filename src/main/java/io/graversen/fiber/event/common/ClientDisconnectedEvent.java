package io.graversen.fiber.event.common;

import io.graversen.fiber.server.management.INetworkClient;

public class ClientDisconnectedEvent extends BaseEvent implements IEvent
{
    private final INetworkClient networkClient;
    private final long clientConnectionTime;
    private final Exception reason;

    public ClientDisconnectedEvent(INetworkClient networkClient, Exception reason)
    {
        this.networkClient = networkClient;
        this.clientConnectionTime = System.currentTimeMillis() - networkClient.connectedAt();
        this.reason = reason;
    }

    public INetworkClient getNetworkClient()
    {
        return networkClient;
    }

    public long getClientConnectionTime()
    {
        return clientConnectionTime;
    }

    public Exception getReason()
    {
        return reason;
    }

    @Override
    public void print()
    {
        System.out.println(String.format("Event - %s - %s: %s (Connected for %d ms.)",
                getClass().getSimpleName(),
                networkClient.connectionTuple(),
                reason.getMessage(),
                clientConnectionTime)
        );
    }
}
