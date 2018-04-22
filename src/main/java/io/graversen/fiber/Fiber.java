package io.graversen.fiber;

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
        TcpServerConfig serverConfig = new TcpServerConfig(1337, "0.0.0.0", 1000, 128, 128);

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
                System.out.println(System.currentTimeMillis());
                event.print();

                //                    server.send(event.getNetworkClient(), new StringBuilder(new String(event.getNetworkMessage().getMessageData())).reverse().toString().getBytes());

                if (new String(event.getNetworkMessage().getMessageData()).equals("test2"))
                {
                    server.send(event.getNetworkClient(), "test reply!".getBytes());
                }

                if (event.getNetworkMessage().sizeInBytes() == 128)
                {
                    server.send(event.getNetworkClient(), event.getNetworkMessage().getMessageData());
                }

                if (new String(event.getNetworkMessage().getMessageData()).equals("q"))
                {
                    server.disconnect(event.getNetworkClient(), new Exception("Get rekt!"));
                }
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
