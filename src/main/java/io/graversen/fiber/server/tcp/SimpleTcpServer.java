package io.graversen.fiber.server.tcp;

import io.graversen.fiber.config.tcp.TcpServerConfig;
import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.server.management.BaseNetworkClientManager;
import io.graversen.fiber.server.tcp.base.BaseTcpServer;

public class SimpleTcpServer extends BaseTcpServer
{
    public SimpleTcpServer(TcpServerConfig serverConfig, BaseNetworkClientManager networkClientManager, IEventBus eventBus)
    {
        super(serverConfig, networkClientManager, eventBus);
    }
}
