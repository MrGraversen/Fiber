package io.graversen.fiber.event.listeners;

import io.graversen.fiber.event.bus.AbstractEventBus;
import io.graversen.fiber.event.common.*;
import io.graversen.fiber.event.listeners.AbstractEventListener;

public abstract class AbstractNetworkClientEventListener
{
    private final AbstractEventBus abstractEventBus;

    protected AbstractNetworkClientEventListener(AbstractEventBus abstractEventBus)
    {
        this.abstractEventBus = abstractEventBus;
        this.abstractEventBus.registerEventListener(ClientConnectedEvent.class, this.clientConnectedListener);
        this.abstractEventBus.registerEventListener(ClientDisconnectedEvent.class, this.clientDisconnectedListener);
        this.abstractEventBus.registerEventListener(NetworkMessageReceivedEvent.class, this.networkMessageReceivedListener);
        this.abstractEventBus.registerEventListener(NetworkMessageSentEvent.class, this.networkMessageSentListener);
        this.abstractEventBus.registerEventListener(ServerReadyEvent.class, this.serverReadyListener);
        this.abstractEventBus.registerEventListener(ServerClosedEvent.class, this.serverClosedListener);
    }

    public abstract void onClientConnected(ClientConnectedEvent clientConnectedEvent);

    public abstract void onClientDisconnected(ClientDisconnectedEvent clientDisconnectedEvent);

    public abstract void onNetworkMessageReceived(NetworkMessageReceivedEvent networkMessageReceivedEvent);

    public abstract void onNetworkMessageSent(NetworkMessageSentEvent networkMessageSentEvent);

    public abstract void onServerReady(ServerReadyEvent serverReadyEvent);

    public abstract void onServerClosed(ServerClosedEvent serverClosedEvent);

    private final AbstractEventListener<ClientConnectedEvent> clientConnectedListener = new AbstractEventListener<ClientConnectedEvent>()
    {
        @Override
        public void onEvent(ClientConnectedEvent event)
        {
            onClientConnected(event);
        }
    };

    private final AbstractEventListener<ClientDisconnectedEvent> clientDisconnectedListener = new AbstractEventListener<ClientDisconnectedEvent>()
    {
        @Override
        public void onEvent(ClientDisconnectedEvent event)
        {
            onClientDisconnected(event);
        }
    };

    private final AbstractEventListener<NetworkMessageReceivedEvent> networkMessageReceivedListener = new AbstractEventListener<NetworkMessageReceivedEvent>()
    {
        @Override
        public void onEvent(NetworkMessageReceivedEvent event)
        {
            onNetworkMessageReceived(event);
        }
    };

    private final AbstractEventListener<NetworkMessageSentEvent> networkMessageSentListener = new AbstractEventListener<NetworkMessageSentEvent>()
    {
        @Override
        public void onEvent(NetworkMessageSentEvent event)
        {
            onNetworkMessageSent(event);
        }
    };

    private final AbstractEventListener<ServerReadyEvent> serverReadyListener = new AbstractEventListener<ServerReadyEvent>()
    {
        @Override
        public void onEvent(ServerReadyEvent event)
        {
            onServerReady(event);
        }
    };

    private final AbstractEventListener<ServerClosedEvent> serverClosedListener = new AbstractEventListener<ServerClosedEvent>()
    {
        @Override
        public void onEvent(ServerClosedEvent event)
        {
            onServerClosed(event);
        }
    };
}
