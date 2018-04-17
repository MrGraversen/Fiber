package io.graversen.fiber.server.websocket.management;

import org.ownzone.lib.network.server.management.INetworkClient;

import java.util.UUID;

public class WebSocketNetworkClient implements INetworkClient
{
    private final UUID id;
    private final String ipAndPort;

    public WebSocketNetworkClient(String ipAndPort)
    {
        this.id = UUID.randomUUID();
        this.ipAndPort = ipAndPort;
    }

    @Override
    public UUID id()
    {
        return id;
    }

    @Override
    public String ipAndPort()
    {
        return ipAndPort;
    }
}
