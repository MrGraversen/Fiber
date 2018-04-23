package io.graversen.fiber.server.websocket;

import io.graversen.fiber.config.base.ServerConfig;
import io.graversen.fiber.event.bus.AbstractEventBus;
import io.graversen.fiber.server.management.AbstractNetworkClientManager;
import io.graversen.fiber.server.websocket.base.AbstractWebSocketServer;

public class SimpleWebSocketServer extends AbstractWebSocketServer
{
    public SimpleWebSocketServer(ServerConfig serverConfig, AbstractNetworkClientManager networkClientManager, AbstractEventBus abstractEventBus)
    {
        super(serverConfig, networkClientManager, abstractEventBus);
    }
}
