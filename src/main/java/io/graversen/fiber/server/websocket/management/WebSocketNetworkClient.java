package io.graversen.fiber.server.websocket.management;

import io.graversen.fiber.server.management.INetworkClient;

import java.util.UUID;

public final class WebSocketNetworkClient implements INetworkClient
{
    private final String id;
    private final String ip;
    private final int port;

    public WebSocketNetworkClient(String ip, int port)
    {
        this.id = UUID.randomUUID().toString();
        this.ip = ip;
        this.port = port;
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
}
