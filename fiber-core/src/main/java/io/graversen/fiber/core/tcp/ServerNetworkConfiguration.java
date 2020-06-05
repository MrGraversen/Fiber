package io.graversen.fiber.core.tcp;

import lombok.NonNull;
import lombok.Value;

import java.net.InetSocketAddress;

@Value
public class ServerNetworkConfiguration {
    private final @NonNull int bindPort;
    private final @NonNull String bindAddress;

    public static ServerNetworkConfiguration localhost(int bindPort) {
        return new ServerNetworkConfiguration(bindPort, "127.0.0.1");
    }

    public static ServerNetworkConfiguration allNetworkInterfaces(int bindPort) {
        return new ServerNetworkConfiguration(bindPort, "0.0.0.0");
    }

    public InetSocketAddress getServerAddress() {
        return getBindAddress() == null ? new InetSocketAddress(getBindPort()) : new InetSocketAddress(getBindAddress(), getBindPort());
    }
}
