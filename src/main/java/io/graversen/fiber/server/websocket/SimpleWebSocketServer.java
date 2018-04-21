package io.graversen.fiber.server.websocket;

import io.graversen.fiber.config.ServerConfig;
import io.graversen.fiber.event.EventBus;
import io.graversen.fiber.server.management.AbstractNetworkClientManager;
import io.graversen.fiber.server.websocket.base.AbstractWebSocketServer;

public class SimpleWebSocketServer extends AbstractWebSocketServer
{
    public SimpleWebSocketServer(ServerConfig serverConfig, AbstractNetworkClientManager networkClientManager, EventBus eventBus)
    {
        super(serverConfig, networkClientManager, eventBus);
    }
}
