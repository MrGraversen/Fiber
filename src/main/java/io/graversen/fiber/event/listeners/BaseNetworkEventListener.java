package io.graversen.fiber.event.listeners;

import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.event.common.*;

public abstract class BaseNetworkEventListener
{
    private final IEventBus eventBus;

    protected BaseNetworkEventListener(IEventBus eventBus)
    {
        this.eventBus = eventBus;
        this.eventBus.registerEventListener(ClientConnectedEvent.class, this.clientConnectedListener);
        this.eventBus.registerEventListener(ClientDisconnectedEvent.class, this.clientDisconnectedListener);
        this.eventBus.registerEventListener(NetworkMessageReceivedEvent.class, this.networkMessageReceivedListener);
        this.eventBus.registerEventListener(NetworkMessageSentEvent.class, this.networkMessageSentListener);
        this.eventBus.registerEventListener(ServerReadyEvent.class, this.serverReadyListener);
        this.eventBus.registerEventListener(ServerClosedEvent.class, this.serverClosedListener);
    }

    public abstract void onClientConnected(ClientConnectedEvent clientConnectedEvent);

    public abstract void onClientDisconnected(ClientDisconnectedEvent clientDisconnectedEvent);

    public abstract void onNetworkMessageReceived(NetworkMessageReceivedEvent networkMessageReceivedEvent);

    public abstract void onNetworkMessageSent(NetworkMessageSentEvent networkMessageSentEvent);

    public abstract void onServerReady(ServerReadyEvent serverReadyEvent);

    public abstract void onServerClosed(ServerClosedEvent serverClosedEvent);

    private final BaseEventListener<ClientConnectedEvent> clientConnectedListener = new BaseEventListener<ClientConnectedEvent>()
    {
        @Override
        public void onEvent(ClientConnectedEvent event)
        {
            onClientConnected(event);
        }
    };

    private final BaseEventListener<ClientDisconnectedEvent> clientDisconnectedListener = new BaseEventListener<ClientDisconnectedEvent>()
    {
        @Override
        public void onEvent(ClientDisconnectedEvent event)
        {
            onClientDisconnected(event);
        }
    };

    private final BaseEventListener<NetworkMessageReceivedEvent> networkMessageReceivedListener = new BaseEventListener<NetworkMessageReceivedEvent>()
    {
        @Override
        public void onEvent(NetworkMessageReceivedEvent event)
        {
            onNetworkMessageReceived(event);
        }
    };

    private final BaseEventListener<NetworkMessageSentEvent> networkMessageSentListener = new BaseEventListener<NetworkMessageSentEvent>()
    {
        @Override
        public void onEvent(NetworkMessageSentEvent event)
        {
            onNetworkMessageSent(event);
        }
    };

    private final BaseEventListener<ServerReadyEvent> serverReadyListener = new BaseEventListener<ServerReadyEvent>()
    {
        @Override
        public void onEvent(ServerReadyEvent event)
        {
            onServerReady(event);
        }
    };

    private final BaseEventListener<ServerClosedEvent> serverClosedListener = new BaseEventListener<ServerClosedEvent>()
    {
        @Override
        public void onEvent(ServerClosedEvent event)
        {
            onServerClosed(event);
        }
    };
}
