package io.graversen.fiber.server.tcp;

import io.graversen.fiber.config.tcp.TcpServerConfig;
import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.server.management.AbstractNetworkClientManager;
import io.graversen.fiber.server.tcp.base.AbstractTcpServer;

public class SimpleTcpServer extends AbstractTcpServer
{
    public SimpleTcpServer(TcpServerConfig serverConfig, AbstractNetworkClientManager networkClientManager, IEventBus eventBus)
    {
        super(serverConfig, networkClientManager, eventBus);
    }
}
