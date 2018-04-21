package io.graversen.fiber;

import io.graversen.fiber.config.base.AllNetworkInterfacesServerConfig;
import io.graversen.fiber.config.base.ServerConfig;
import io.graversen.fiber.config.tcp.TcpServerConfig;
import io.graversen.fiber.event.*;
import io.graversen.fiber.server.base.AbstractNetworkingServer;
import io.graversen.fiber.server.management.DefaultNetworkClientManager;
import io.graversen.fiber.server.tcp.SimpleTcpServer;

public class Fiber
{
    public static void main(String[] args)
    {
        DefaultNetworkClientManager defaultNetworkClientManager = new DefaultNetworkClientManager();
//        ServerConfig serverConfig = new AllNetworkInterfacesServerConfig(1337);
        TcpServerConfig serverConfig = new TcpServerConfig(1337, "0.0.0.0", 1000, 1024, 1024);

        EventBus eventBus = new EventBus();

//        AbstractNetworkingServer server = new SimpleWebSocketServer(serverConfig, defaultNetworkClientManager, eventBus);
        AbstractNetworkingServer server = new SimpleTcpServer(serverConfig, defaultNetworkClientManager, eventBus);

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
        eventBus.registerEventListener(NetworkMessageReceivedEvent.class, new AbstractEventListener<NetworkMessageReceivedEvent>()
        {
            @Override
            public void onEvent(NetworkMessageReceivedEvent event)
            {
                event.print();
                server.send(event.getNetworkClient(), new StringBuilder(new String(event.getNetworkMessage().getMessageData())).reverse().toString().getBytes());
            }
        });
        eventBus.registerEventListener(NetworkMessageSentEvent.class, new AbstractEventListener<NetworkMessageSentEvent>()
        {
            @Override
            public void onEvent(NetworkMessageSentEvent event)
            {
                event.print();
            }
        });
        eventBus.registerEventListener(ServerErrorEvent.class, new AbstractEventListener<ServerErrorEvent>()
        {
            @Override
            public void onEvent(ServerErrorEvent event)
            {
                event.print();
            }
        });

        server.start();
    }
}
