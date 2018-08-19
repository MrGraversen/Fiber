package io.graversen.fiber.server.websocket;

import io.graversen.fiber.config.base.ServerConfig;
import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.server.management.BaseNetworkClientManager;
import io.graversen.fiber.server.websocket.base.BaseWebSocketServer;

public class SimpleWebSocketServer extends BaseWebSocketServer
{
    public SimpleWebSocketServer(ServerConfig serverConfig, BaseNetworkClientManager networkClientManager, IEventBus eventBus)
    {
        super(serverConfig, networkClientManager, eventBus);
    }
}
