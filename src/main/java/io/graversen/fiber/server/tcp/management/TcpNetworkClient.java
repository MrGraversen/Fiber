package io.graversen.fiber.server.tcp.management;

import io.graversen.fiber.server.management.INetworkClient;
import io.graversen.fiber.server.management.NetworkMessage;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class TcpNetworkClient implements INetworkClient
{
    private final SocketChannel socketChannel;
    private final SelectionKey selectionKey;
    private final ConcurrentLinkedQueue<NetworkMessage> networkWriteQueue;

    private final String id;
    private final String ip;
    private final int port;

    public TcpNetworkClient(SocketChannel socketChannel, SelectionKey selectionKey, String ip, int port)
    {
        this.socketChannel = socketChannel;
        this.selectionKey = selectionKey;
        this.networkWriteQueue = new ConcurrentLinkedQueue<>();

        this.id = UUID.randomUUID().toString();
        this.ip = ip;
        this.port = port;
    }

    public SocketChannel getSocketChannel()
    {
        return socketChannel;
    }

    public SelectionKey getSelectionKey()
    {
        return selectionKey;
    }

    public void putOnNetworkQueue(NetworkMessage networkMessage)
    {
        networkWriteQueue.offer(networkMessage);
    }

    public Optional<NetworkMessage> pollFromNetworkQueue()
    {
        return Optional.ofNullable(networkWriteQueue.poll());
    }

    public List<NetworkMessage> pollAllFromNetworkQueue()
    {
        final List<NetworkMessage> networkMessages = new ArrayList<>();

        Optional<NetworkMessage> networkMessage = Optional.empty();
        while ((networkMessage = pollFromNetworkQueue()).isPresent())
        {
            networkMessages.add(networkMessage.get());
        }

        return networkMessages;
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
