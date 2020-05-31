package io.graversen.fiber.core.tcp;

import io.graversen.fiber.core.INetworkEngine;

import java.io.IOException;
import java.nio.channels.AsynchronousServerSocketChannel;

public interface ITcpNetworkEngine extends INetworkEngine {
    void start(NetworkAcceptHandler networkAcceptHandler) throws IOException;

    void stop();

    ServerNetworkConfiguration getNetworkConfiguration();

    AsynchronousServerSocketChannel getServerSocketChannel();
}
