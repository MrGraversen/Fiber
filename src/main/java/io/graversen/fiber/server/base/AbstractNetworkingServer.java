package io.graversen.fiber.server.base;

import io.graversen.fiber.config.base.ServerConfig;
import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.server.management.AbstractNetworkClientManager;
import io.graversen.fiber.server.management.INetworkClient;

public abstract class AbstractNetworkingServer
{
    private final AbstractNetworkClientManager networkClientManager;
    private final IEventBus eventBus;
    private final ServerConfig serverConfig;

    public AbstractNetworkingServer(ServerConfig serverConfig, AbstractNetworkClientManager networkClientManager, IEventBus eventBus)
    {
        this.serverConfig = serverConfig;
        this.networkClientManager = networkClientManager;
        this.eventBus = eventBus;

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
        {
            public void run()
            {
                System.out.println(String.format("JVM Shutdown Hook - %s", getClass().getSimpleName()));
                stop(new RuntimeException("Server Closing"), true);
            }
        }));
    }

    public AbstractNetworkClientManager getNetworkClientManager()
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
