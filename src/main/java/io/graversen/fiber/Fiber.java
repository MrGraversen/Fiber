package io.graversen.fiber;

import io.graversen.fiber.config.AllNetworkInterfacesServerConfig;
import io.graversen.fiber.config.ServerConfig;
import io.graversen.fiber.event.*;
import io.graversen.fiber.server.management.DefaultNetworkClientManager;
import io.graversen.fiber.server.websocket.SimpleWebSocketServer;

public class Fiber
{
    public static void main(String[] args)
    {
        DefaultNetworkClientManager defaultNetworkClientManager = new DefaultNetworkClientManager();
        ServerConfig serverConfig = new AllNetworkInterfacesServerConfig(1337);

        EventBus eventBus = new EventBus();
        eventBus.registerEventListener(ServerReadyEvent.class, new AbstractEventListener<ServerReadyEvent>()
        {
            @Override
            public void onEvent(ServerReadyEvent event)
            {
                event.print();
            }
        });
        eventBus.registerEventListener(ClientConnectedEvent.class, new AbstractEventListener<ClientConnectedEvent>()
        {
            @Override
            public void onEvent(ClientConnectedEvent event)
            {
                event.print();
            }
        });
        eventBus.registerEventListener(ClientDisconnectedEvent.class, new AbstractEventListener<ClientDisconnectedEvent>()
        {
            @Override
            public void onEvent(ClientDisconnectedEvent event)
            {
                event.print();
            }
        });
        eventBus.registerEventListener(NetworkMessageEvent.class, new AbstractEventListener<NetworkMessageEvent>()
        {
            @Override
            public void onEvent(NetworkMessageEvent event)
            {
                event.print();
            }
        });

        SimpleWebSocketServer simpleWebSocketServer = new SimpleWebSocketServer(serverConfig, defaultNetworkClientManager, eventBus);
        simpleWebSocketServer.start();
    }
}
