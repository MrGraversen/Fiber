package io.graversen.fiber;

import io.graversen.fiber.config.tcp.TcpServerConfig;
import io.graversen.fiber.event.bus.AbstractEventBus;
import io.graversen.fiber.event.bus.DefaultEventBus;
import io.graversen.fiber.event.common.*;
import io.graversen.fiber.event.listeners.AbstractEventListener;
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

        AbstractEventBus abstractEventBus = new DefaultEventBus();

//        AbstractNetworkingServer server = new SimpleWebSocketServer(serverConfig, defaultNetworkClientManager, eventBus);
        AbstractNetworkingServer server = new SimpleTcpServer(serverConfig, defaultNetworkClientManager, abstractEventBus);

        abstractEventBus.registerEventListener(ServerReadyEvent.class, new AbstractEventListener<ServerReadyEvent>()
        {
            @Override
            public void onEvent(ServerReadyEvent event)
            {
                event.print();
            }
        });
        abstractEventBus.registerEventListener(ServerClosedEvent.class, new AbstractEventListener<ServerClosedEvent>()
        {
            @Override
            public void onEvent(ServerClosedEvent event)
            {
                event.print();
            }
        });
        abstractEventBus.registerEventListener(ClientConnectedEvent.class, new AbstractEventListener<ClientConnectedEvent>()
        {
            @Override
            public void onEvent(ClientConnectedEvent event)
            {
                event.print();
            }
        });
        abstractEventBus.registerEventListener(ClientDisconnectedEvent.class, new AbstractEventListener<ClientDisconnectedEvent>()
        {
            @Override
            public void onEvent(ClientDisconnectedEvent event)
            {
                event.print();
            }
        });
        abstractEventBus.registerEventListener(NetworkMessageReceivedEvent.class, new AbstractEventListener<NetworkMessageReceivedEvent>()
        {
            @Override
            public void onEvent(NetworkMessageReceivedEvent event)
            {
                event.print();

                //server.send(event.getNetworkClient(), new StringBuilder(new String(event.getNetworkMessage().getMessageData())).reverse().toString().getBytes());

                if (new String(event.getNetworkMessage().getMessageData()).equals("test1"))
                {
                    server.broadcast("test broadcast boys!".getBytes());
                }

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
                    server.disconnect(event.getNetworkClient(), new Exception("Get disconnected boiii"));
                }
            }
        });
        abstractEventBus.registerEventListener(NetworkMessageSentEvent.class, new AbstractEventListener<NetworkMessageSentEvent>()
        {
            @Override
            public void onEvent(NetworkMessageSentEvent event)
            {
                event.print();
            }
        });
        abstractEventBus.registerEventListener(ServerErrorEvent.class, new AbstractEventListener<ServerErrorEvent>()
        {
            @Override
            public void onEvent(ServerErrorEvent event)
            {
                event.print();
            }
        });

        server.start();
        server.stop(new RuntimeException("Yes ses"), true);
    }
}
