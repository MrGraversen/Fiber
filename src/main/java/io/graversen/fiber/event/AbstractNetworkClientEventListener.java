package io.graversen.fiber.event;

import org.ownzone.lib.network.server.management.INetworkClient;

public abstract class AbstractNetworkClientEventListener
{
    private final EventBus eventBus;

    protected AbstractNetworkClientEventListener(EventBus eventBus)
    {
        this.eventBus = eventBus;
        this.eventBus.registerEventListener(ClientConnectedEvent.class, this.clientConnectedListener);
        this.eventBus.registerEventListener(ClientDisconnectedEvent.class, this.clientDisconnectedListener);
        // TODO network message
        this.eventBus.registerEventListener(ServerReadyEvent.class, this.serverReadyListener);
    }

    public abstract void onClientConnected(INetworkClient networkClient);

    public abstract void onClientDisconnected(INetworkClient networkClient);

    public abstract void onNetworkMessage();

    public abstract void onServerReady();

    private final AbstractEventListener<ClientConnectedEvent> clientConnectedListener = new AbstractEventListener<ClientConnectedEvent>()
    {
        @Override
        public void onEvent(ClientConnectedEvent event)
        {
            onClientConnected(event.getNetworkClient());
        }
    };

    private final AbstractEventListener<ClientDisconnectedEvent> clientDisconnectedListener = new AbstractEventListener<ClientDisconnectedEvent>()
    {
        @Override
        public void onEvent(ClientDisconnectedEvent event)
        {
            onClientDisconnected(event.getNetworkClient());
        }
    };

    // TODO network message

    private final AbstractEventListener<ServerReadyEvent> serverReadyListener = new AbstractEventListener<ServerReadyEvent>()
    {
        @Override
        public void onEvent(ServerReadyEvent event)
        {
            onServerReady();
        }
    };
}
