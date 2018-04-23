package io.graversen.fiber.event.common;

import io.graversen.fiber.server.management.INetworkClient;
import io.graversen.fiber.server.management.NetworkMessage;

public class NetworkMessageSentEvent extends BaseEvent implements IEvent
{
    private final INetworkClient networkClient;
    private final NetworkMessage networkMessage;

    public NetworkMessageSentEvent(INetworkClient networkClient, NetworkMessage networkMessage)
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
        System.out.println(String.format("Event - %s - %s (%d bytes): %s", getClass().getSimpleName(), networkClient.connectionTuple(), networkMessage.sizeInBytes(), new String(networkMessage.getMessageData())));
    }
}
