package io.graversen.fiber.server.base;

import io.graversen.fiber.config.ServerConfig;
import io.graversen.fiber.event.EventBus;
import io.graversen.fiber.server.management.AbstractNetworkClientManager;

public abstract class AbstractNetworkingServer
{
    private final AbstractNetworkClientManager networkClientManager;
    private final EventBus eventBus;
    private final ServerConfig serverConfig;

    public AbstractNetworkingServer(ServerConfig serverConfig, AbstractNetworkClientManager networkClientManager, EventBus eventBus)
    {
        this.serverConfig = serverConfig;
        this.networkClientManager = networkClientManager;
        this.eventBus = eventBus;
    }

    public AbstractNetworkClientManager getNetworkClientManager()
    {
        return networkClientManager;
    }

    public ServerConfig getServerConfig()
    {
        return serverConfig;
    }

    public EventBus getEventBus()
    {
        return eventBus;
    }

    public abstract void start();

    public abstract void stop(Exception reason, boolean gently);
}
