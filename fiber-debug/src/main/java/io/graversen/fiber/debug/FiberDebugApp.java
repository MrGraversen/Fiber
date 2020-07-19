package io.graversen.fiber.debug;

import io.graversen.fiber.core.tcp.ServerNetworkConfiguration;
import io.graversen.fiber.event.bus.DefaultEventBus;

public class FiberDebugApp {
    public static void main(String[] args) {
        final var debugTcpPlatform = new DebugTcpPlatform(new DefaultEventBus());
        final var bytesEncodeContext = debugTcpPlatform.registerEncoder(new ByteToByteEncoder());
        debugTcpPlatform.registerDecoder(new ByteToByteDecoder(), new EchoingReceiver(bytesEncodeContext));
        debugTcpPlatform.start(ServerNetworkConfiguration.allNetworkInterfaces(1337));
    }
}
