package io.graversen.fiber.server.websocket.base;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.ownzone.lib.network.event.EventBus;
import org.ownzone.lib.network.event.ServerReadyEvent;
import org.ownzone.lib.network.server.base.AbstractNetworkingServer;
import org.ownzone.lib.network.server.management.AbstractNetworkClientManager;

public abstract class AbstractWebSocketServer extends AbstractNetworkingServer
{
    public AbstractWebSocketServer(AbstractNetworkClientManager networkClientManager, EventBus eventBus)
    {
        super(networkClientManager, eventBus);
    }

    private class WsServer extends WebSocketServer
    {
        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake)
        {

        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote)
        {

        }

        @Override
        public void onMessage(WebSocket conn, String message)
        {

        }

        @Override
        public void onError(WebSocket conn, Exception ex)
        {

        }

        @Override
        public void onStart()
        {
            eventBus.publishEvent(new ServerReadyEvent());
        }
    }
}