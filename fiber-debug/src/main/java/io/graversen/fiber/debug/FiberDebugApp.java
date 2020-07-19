package io.graversen.fiber.debug;

import io.graversen.fiber.core.tcp.ServerNetworkConfiguration;
import io.graversen.fiber.event.bus.DefaultEventBus;

public class FiberDebugApp {
    public static void main(String[] args) {
        final var debugTcpPlatform = new DebugTcpPlatform(new DefaultEventBus());
        debugTcpPlatform.start(ServerNetworkConfiguration.allNetworkInterfaces(1337));
    }
}
