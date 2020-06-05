package io.graversen.fiber.core.tcp;

import lombok.NonNull;
import lombok.Value;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;

@Value
public class ClientNetworkDetails {
    private final @NonNull String remoteIpAddress;
    private final @NonNull int remotePort;

    public static ClientNetworkDetails from(AsynchronousSocketChannel socketChannel) {
        try {
            if (socketChannel.getRemoteAddress() instanceof InetSocketAddress) {
                final var iNetSocketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                return new ClientNetworkDetails(iNetSocketAddress.getAddress().getHostAddress(), iNetSocketAddress.getPort());
            } else {
                return new ClientNetworkDetails(socketChannel.getRemoteAddress().toString(), 0);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String connectionTuple() {
        return String.format("%s:%d", getRemoteIpAddress(), getRemotePort());
    }
}
