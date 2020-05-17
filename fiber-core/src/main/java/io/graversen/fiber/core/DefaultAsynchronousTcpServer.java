package io.graversen.fiber.core;

import io.graversen.fiber.event.bus.IEventBus;

public class DefaultAsynchronousTcpServer extends AsynchronousTcpServer {
    public DefaultAsynchronousTcpServer(
            ServerNetworkConfiguration serverNetworkConfiguration,
            ServerInternalsConfiguration serverInternalsConfiguration,
            IEventBus eventBus,
            ITcpNetworkClientRepository networkClientRepository
    ) {
        super(serverNetworkConfiguration, serverInternalsConfiguration, eventBus, networkClientRepository);
    }
}
