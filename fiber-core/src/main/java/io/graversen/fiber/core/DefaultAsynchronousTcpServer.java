package io.graversen.fiber.core;

import io.graversen.fiber.event.bus.DefaultEventBus;

public class DefaultAsynchronousTcpServer extends AsynchronousTcpServer {
    public DefaultAsynchronousTcpServer(ServerNetworkConfiguration serverNetworkConfiguration) {
        super(
                serverNetworkConfiguration,
                new ServerInternalsConfiguration(8192, 25),
                new DefaultEventBus(),
                new DefaultTcpClientRepository()
        );
    }
}
