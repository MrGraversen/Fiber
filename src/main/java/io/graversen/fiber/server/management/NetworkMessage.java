package io.graversen.fiber.server.management;

public final class NetworkMessage
{
    private final byte[] messageData;
    private final INetworkClient networkClient;

    public NetworkMessage(byte[] messageData, INetworkClient networkClient)
    {
        this.messageData = messageData;
        this.networkClient = networkClient;
    }

    public byte[] getMessageData()
    {
        return messageData;
    }

    public INetworkClient getNetworkClient()
    {
        return networkClient;
    }
}
