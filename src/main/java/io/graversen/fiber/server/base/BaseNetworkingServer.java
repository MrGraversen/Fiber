package io.graversen.fiber.server.base;

import io.graversen.fiber.config.base.ServerConfig;
import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.server.management.BaseNetworkClientManager;
import io.graversen.fiber.server.management.INetworkClient;

public abstract class BaseNetworkingServer
{
    private final BaseNetworkClientManager networkClientManager;
    private final IEventBus eventBus;
    private final ServerConfig serverConfig;

    public BaseNetworkingServer(ServerConfig serverConfig, BaseNetworkClientManager networkClientManager, IEventBus eventBus)
    {
        this.serverConfig = serverConfig;
        this.networkClientManager = networkClientManager;
        this.eventBus = eventBus;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> stop(new RuntimeException("Server Closing"), true)));
    }

    public BaseNetworkClientManager getNetworkClientManager()
    {
        return networkClientManager;
    }

    public ServerConfig getServerConfig()
    {
        return serverConfig;
    }

    public IEventBus getEventBus()
    {
        return eventBus;
    }

    public abstract void start();

    public abstract void stop(Exception reason, boolean gently);

    public abstract void broadcast(byte[] messageData);

    public abstract void disconnect(String networkClientId, Exception reason);

    public abstract void disconnect(INetworkClient networkClient, Exception reason);

    public abstract void send(String networkClientId, byte[] messageData);

    public abstract void send(INetworkClient networkClient, byte[] messageData);
}
