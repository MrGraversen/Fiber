package io.graversen.fiber.event;

import io.graversen.fiber.server.management.INetworkClient;

public class ClientConnectedEvent extends BaseEvent implements IEvent
{
    private final INetworkClient networkClient;

    public ClientConnectedEvent(INetworkClient networkClient)
    {
        this.networkClient = networkClient;
    }

    public INetworkClient getNetworkClient()
    {
        return networkClient;
    }

    @Override
    public void print()
    {
        System.out.println(String.format("Event - %s - %s", getClass().getSimpleName(), networkClient.connectionTuple()));
    }
}
