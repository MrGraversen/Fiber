package io.graversen.fiber.server.websocket.base;

import io.graversen.fiber.config.base.ServerConfig;
import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.event.common.*;
import io.graversen.fiber.server.management.INetworkClient;
import io.graversen.fiber.server.management.NetworkMessage;
import io.graversen.fiber.server.websocket.management.WebSocketNetworkClient;
import io.graversen.fiber.util.NetworkUtil;
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

    public AbstractWebSocketServer(ServerConfig serverConfig, AbstractNetworkClientManager networkClientManager, IEventBus eventBus)
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

        final ServerClosedEvent serverClosedEvent = new ServerClosedEvent(this, reason);
        getEventBus().emitEvent(serverClosedEvent, true);
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
        final WebSocketNetworkClient webSocketClient = (WebSocketNetworkClient) networkClient;

        if (webSocketClient.getWebSocket().isOpen())
        {
            webSocketClient.getWebSocket().send(new String(messageData));

            final NetworkMessageSentEvent networkMessageSentEvent = new NetworkMessageSentEvent(networkClient, new NetworkMessage(messageData));
            getEventBus().emitEvent(networkMessageSentEvent, true);
        }
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
            getEventBus().emitEvent(clientConnectedEvent, true);
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote)
        {
            final Optional<INetworkClient> webSocketNetworkClient = getNetworkClientManager().getClientByConnectionTuple(getConnectionTuple(conn));

            webSocketNetworkClient.ifPresent(networkClient -> {
                getNetworkClientManager().deleteClient(networkClient);

                final ClientDisconnectedEvent clientDisconnectedEvent = new ClientDisconnectedEvent(networkClient, new IOException(reason));
                getEventBus().emitEvent(clientDisconnectedEvent, true);
            });
        }

        @Override
        public void onMessage(WebSocket conn, String message)
        {
            final Optional<INetworkClient> webSocketNetworkClient = getNetworkClientManager().getClientByConnectionTuple(getConnectionTuple(conn));

            webSocketNetworkClient.ifPresent(networkClient -> {
                final NetworkMessage networkMessage = new NetworkMessage(message.getBytes());

                final NetworkMessageReceivedEvent networkMessageReceivedEvent = new NetworkMessageReceivedEvent(networkClient, networkMessage);
                getEventBus().emitEvent(networkMessageReceivedEvent, true);
            });
        }

        @Override
        public void onError(WebSocket conn, Exception ex)
        {
            final ServerErrorEvent serverErrorEvent = new ServerErrorEvent(abstractWebSocketServer, ex);
            getEventBus().emitEvent(serverErrorEvent, true);
        }

        @Override
        public void onStart()
        {
            final ServerReadyEvent serverReadyEvent = new ServerReadyEvent(abstractWebSocketServer);
            getEventBus().emitEvent(serverReadyEvent, true);
        }

        private String getConnectionTuple(WebSocket webSocket)
        {
            return NetworkUtil.getConnectionTuple(webSocket.getRemoteSocketAddress().getHostName(), webSocket.getRemoteSocketAddress().getPort());
        }
    }
}