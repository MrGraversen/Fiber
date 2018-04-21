package io.graversen.fiber.config.tcp;

import io.graversen.fiber.config.base.ServerConfig;

public class TcpServerConfig extends ServerConfig
{
    private final int serverAcceptTimeoutMs;
    private final int clientSendBufferBytes;
    private final int clientReceiveBufferBytes;

    public TcpServerConfig(int bindPort, String bindAddress, int serverAcceptTimeoutMs, int clientSendBufferBytes, int clientReceiveBufferBytes)
    {
        super(bindPort, bindAddress);
        this.serverAcceptTimeoutMs = serverAcceptTimeoutMs;
        this.clientSendBufferBytes = clientSendBufferBytes;
        this.clientReceiveBufferBytes = clientReceiveBufferBytes;
    }

    public int getServerAcceptTimeoutMs()
    {
        return serverAcceptTimeoutMs;
    }

    public int getClientSendBufferBytes()
    {
        return clientSendBufferBytes;
    }

    public int getClientReceiveBufferBytes()
    {
        return clientReceiveBufferBytes;
    }
}
