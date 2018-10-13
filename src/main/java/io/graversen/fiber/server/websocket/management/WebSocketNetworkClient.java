package io.graversen.fiber.server.websocket.management;

import io.graversen.fiber.server.management.INetworkClient;
import org.java_websocket.WebSocket;

import java.util.UUID;

public final class WebSocketNetworkClient implements INetworkClient
{
    private final WebSocket webSocket;
    private final String id;
    private final String ip;
    private final int port;
    private final long connectedAt;

    public WebSocketNetworkClient(WebSocket webSocket, String ip, int port)
    {
        this.webSocket = webSocket;
        this.id = UUID.randomUUID().toString();
        this.ip = ip;
        this.port = port;
        this.connectedAt = System.currentTimeMillis();
    }

    public WebSocket getWebSocket()
    {
        return webSocket;
    }

    @Override
    public String id()
    {
        return id;
    }

    @Override
    public String ipAddress()
    {
        return ip;
    }

    @Override
    public int port()
    {
        return port;
    }

    @Override
    public long connectedAt()
    {
        return connectedAt;
    }
}
