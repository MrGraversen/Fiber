package io.graversen.fiber.event.listeners.reference;

import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.event.common.*;
import io.graversen.fiber.event.listeners.BaseNetworkEventListener;

public class PrintingEventListener extends BaseNetworkEventListener
{
    public PrintingEventListener(IEventBus eventBus)
    {
        super(eventBus);
    }

    @Override
    public void onClientConnected(ClientConnectedEvent event)
    {
        event.print();
    }

    @Override
    public void onClientDisconnected(ClientDisconnectedEvent event)
    {
        event.print();
    }

    @Override
    public void onNetworkMessageReceived(NetworkMessageReceivedEvent event)
    {
        event.print();
    }

    @Override
    public void onNetworkMessageSent(NetworkMessageSentEvent event)
    {
        event.print();
    }

    @Override
    public void onServerReady(ServerReadyEvent event)
    {
        event.print();
    }

    @Override
    public void onServerClosed(ServerClosedEvent event)
    {
        event.print();
    }
}
