package io.graversen.fiber.event;

import io.graversen.fiber.server.management.INetworkClient;
import io.graversen.fiber.server.management.NetworkMessage;

public class NetworkMessageEvent extends BaseEvent implements IEvent
{
    private final INetworkClient networkClient;
    private final NetworkMessage networkMessage;

    public NetworkMessageEvent(INetworkClient networkClient, NetworkMessage networkMessage)
    {
        this.networkClient = networkClient;
        this.networkMessage = networkMessage;
    }

    public INetworkClient getNetworkClient()
    {
        return networkClient;
    }

    public NetworkMessage getNetworkMessage()
    {
        return networkMessage;
    }

    @Override
    public void print()
    {
        System.out.println(String.format("Event - %s - %s: %s", getClass().getSimpleName(), networkClient.connectionTuple(), new String(networkMessage.getMessageData())));
    }
}
