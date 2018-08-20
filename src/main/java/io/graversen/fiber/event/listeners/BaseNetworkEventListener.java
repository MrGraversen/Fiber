package io.graversen.fiber.event.listeners;

import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.event.common.*;

public abstract class BaseNetworkEventListener
{
    private final IEventBus eventBus;

    private final IEventListener<ClientConnectedEvent> clientConnectedListener = this::onClientConnected;
    private final IEventListener<ClientDisconnectedEvent> clientDisconnectedListener = this::onClientDisconnected;
    private final IEventListener<NetworkMessageReceivedEvent> networkMessageReceivedListener = this::onNetworkMessageReceived;
    private final IEventListener<NetworkMessageSentEvent> networkMessageSentListener = this::onNetworkMessageSent;
    private final IEventListener<ServerReadyEvent> serverReadyListener = this::onServerReady;
    private final IEventListener<ServerClosedEvent> serverClosedListener = this::onServerClosed;

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
}
