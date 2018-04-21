package io.graversen.fiber.server.websocket.base;

import io.graversen.fiber.config.base.ServerConfig;
import io.graversen.fiber.event.*;
import io.graversen.fiber.server.management.INetworkClient;
import io.graversen.fiber.server.management.NetworkMessage;
import io.graversen.fiber.server.websocket.management.WebSocketNetworkClient;
import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import io.graversen.fiber.server.base.AbstractNetworkingServer;
import io.graversen.fiber.server.management.AbstractNetworkClientManager;

import java.io.IOException;
import java.util.Optional;

public abstract class AbstractWebSocketServer extends AbstractNetworkingServer
{
    private WsServer wsServer;

    public AbstractWebSocketServer(ServerConfig serverConfig, AbstractNetworkClientManager networkClientManager, EventBus eventBus)
    {
        super(serverConfig, networkClientManager, eventBus);
        this.wsServer = new WsServer(this);
    }

    @Override
    public void start()
    {
        this.wsServer.start();
    }

    @Override
    public void stop(Exception reason, boolean gently)
    {
        try
        {
            if (gently)
            {
                getNetworkClientManager().getAllClients().forEach(networkClient -> disconnect(networkClient, reason));
            }

            this.wsServer.stop();
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }

        final ServerClosedEvent serverClosedEvent = new ServerClosedEvent(this);
        getEventBus().publishEvent(serverClosedEvent, true);
    }

    @Override
    public void broadcast(byte[] messageData)
    {
        this.wsServer.broadcast(messageData);
    }

    @Override
    public void disconnect(String networkClientId, Exception reason)
    {
        final Optional<INetworkClient> networkClient = getNetworkClientManager().getClient(networkClientId);
        networkClient.ifPresent(client -> disconnect(client, reason));
    }

    @Override
    public void disconnect(INetworkClient networkClient, Exception reason)
    {
        ((WebSocketNetworkClient) networkClient).getWebSocket().close(CloseFrame.NORMAL, reason.getMessage());
    }

    @Override
    public void send(String networkClientId, byte[] messageData)
    {
        final Optional<INetworkClient> networkClient = getNetworkClientManager().getClient(networkClientId);
        networkClient.ifPresent(client -> send(client, messageData));
    }

    @Override
    public void send(INetworkClient networkClient, byte[] messageData)
    {
        ((WebSocketNetworkClient) networkClient).getWebSocket().send(new String(messageData));

        final NetworkMessageSentEvent networkMessageSentEvent = new NetworkMessageSentEvent(networkClient, new NetworkMessage(messageData));
        getEventBus().publishEvent(networkMessageSentEvent, true);
    }

    private class WsServer extends WebSocketServer
    {
        private final AbstractWebSocketServer abstractWebSocketServer;

        public WsServer(AbstractWebSocketServer abstractWebSocketServer)
        {
            super(abstractWebSocketServer.getServerConfig().getServerAddress());
            this.abstractWebSocketServer = abstractWebSocketServer;
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake)
        {
            final WebSocketNetworkClient webSocketNetworkClient = new WebSocketNetworkClient(conn, conn.getRemoteSocketAddress().getHostName(), conn.getRemoteSocketAddress().getPort());
            getNetworkClientManager().storeClient(webSocketNetworkClient);

            final ClientConnectedEvent clientConnectedEvent = new ClientConnectedEvent(webSocketNetworkClient);
            getEventBus().publishEvent(clientConnectedEvent, true);
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote)
        {
            final WebSocketNetworkClient webSocketNetworkClient = new WebSocketNetworkClient(conn, conn.getRemoteSocketAddress().getHostName(), conn.getRemoteSocketAddress().getPort());
            getNetworkClientManager().deleteClient(webSocketNetworkClient);

            final ClientDisconnectedEvent clientDisconnectedEvent = new ClientDisconnectedEvent(webSocketNetworkClient, new IOException(reason));
            getEventBus().publishEvent(clientDisconnectedEvent, true);
        }

        @Override
        public void onMessage(WebSocket conn, String message)
        {
            final WebSocketNetworkClient webSocketNetworkClient = new WebSocketNetworkClient(conn, conn.getRemoteSocketAddress().getHostName(), conn.getRemoteSocketAddress().getPort());
            final NetworkMessage networkMessage = new NetworkMessage(message.getBytes());

            final NetworkMessageReceivedEvent networkMessageReceivedEvent = new NetworkMessageReceivedEvent(webSocketNetworkClient, networkMessage);
            getEventBus().publishEvent(networkMessageReceivedEvent, true);
        }

        @Override
        public void onError(WebSocket conn, Exception ex)
        {
            // FIXME
            ex.printStackTrace();
        }

        @Override
        public void onStart()
        {
            final ServerReadyEvent serverReadyEvent = new ServerReadyEvent(abstractWebSocketServer);
            getEventBus().publishEvent(serverReadyEvent, true);
        }
    }
}