package io.graversen.fiber.server.tcp;

import io.graversen.fiber.config.base.ServerConfig;
import io.graversen.fiber.config.tcp.TcpServerConfig;
import io.graversen.fiber.event.EventBus;
import io.graversen.fiber.server.management.AbstractNetworkClientManager;
import io.graversen.fiber.server.tcp.base.AbstractTcpServer;

public class SimpleTcpServer extends AbstractTcpServer
{
    public SimpleTcpServer(TcpServerConfig serverConfig, AbstractNetworkClientManager networkClientManager, EventBus eventBus)
    {
        super(serverConfig, networkClientManager, eventBus);
    }
}
