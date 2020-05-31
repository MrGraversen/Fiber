package io.graversen.fiber.core.tcp;

import io.graversen.fiber.utils.ChannelUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
public class AsyncTcpNetworkEngine implements ITcpNetworkEngine {
    private final @NonNull ServerNetworkConfiguration networkConfiguration;

    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel serverSocketChannel;

    @Override
    public void start(NetworkAcceptHandler networkAcceptHandler) throws IOException {
        channelGroup = AsynchronousChannelGroup.withThreadPool(Executors.newCachedThreadPool());
        serverSocketChannel = AsynchronousServerSocketChannel
                .open(channelGroup)
                .bind(networkConfiguration.getServerAddress());

        serverSocketChannel.accept(null, networkAcceptHandler);
    }

    @Override
    public void stop() {
        channelGroup.shutdown();
        ChannelUtils.close(serverSocketChannel);
    }

    @Override
    public ServerNetworkConfiguration getNetworkConfiguration() {
        return networkConfiguration;
    }

    @Override
    public AsynchronousServerSocketChannel getServerSocketChannel() {
        return serverSocketChannel;
    }

    @Override
    public int getBindPort() {
        return networkConfiguration.getBindPort();
    }
}
