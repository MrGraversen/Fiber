package io.graversen.fiber.server.tcp.management;

import io.graversen.fiber.server.management.INetworkClient;

import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.UUID;

public final class TcpNetworkClient implements INetworkClient
{
    private final SocketChannel socketChannel;
    private final String id;
    private final String ip;
    private final int port;

    public TcpNetworkClient(SocketChannel socketChannel, String ip, int port)
    {
        this.socketChannel = socketChannel;
        this.id = UUID.randomUUID().toString();
        this.ip = ip;
        this.port = port;
    }

    public SocketChannel getSocketChannel()
    {
        return socketChannel;
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
