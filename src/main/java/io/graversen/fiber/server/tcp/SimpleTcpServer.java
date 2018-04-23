package io.graversen.fiber.server.tcp;

import io.graversen.fiber.config.tcp.TcpServerConfig;
import io.graversen.fiber.event.bus.AbstractEventBus;
import io.graversen.fiber.server.management.AbstractNetworkClientManager;
import io.graversen.fiber.server.tcp.base.AbstractTcpServer;

public class SimpleTcpServer extends AbstractTcpServer
{
    public SimpleTcpServer(TcpServerConfig serverConfig, AbstractNetworkClientManager networkClientManager, AbstractEventBus abstractEventBus)
    {
        super(serverConfig, networkClientManager, abstractEventBus);
    }
}
