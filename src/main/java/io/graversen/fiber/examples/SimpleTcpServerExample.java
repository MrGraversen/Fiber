package io.graversen.fiber.examples;

import io.graversen.fiber.config.tcp.AllNetworkInterfacesTcpServerConfig;
import io.graversen.fiber.config.tcp.TcpServerConfig;
import io.graversen.fiber.event.bus.AbstractEventBus;
import io.graversen.fiber.event.bus.DefaultEventBus;
import io.graversen.fiber.event.common.*;
import io.graversen.fiber.event.listeners.AbstractEventListener;
import io.graversen.fiber.event.listeners.AbstractNetworkEventListener;
import io.graversen.fiber.server.base.AbstractNetworkingServer;
import io.graversen.fiber.server.management.AbstractNetworkClientManager;
import io.graversen.fiber.server.management.DefaultNetworkClientManager;
import io.graversen.fiber.server.tcp.SimpleTcpServer;

public class SimpleTcpServerExample
{
    public static void main(String[] args)
    {
        // First, let's configure the TCP server instance - will listen on port 1337
        final TcpServerConfig tcpServerConfig = new AllNetworkInterfacesTcpServerConfig(1337);

        // Declare an implementation of the Event Bus
        final AbstractEventBus eventBus = new DefaultEventBus();

        // Declare an implementation of Network Client Manager
        final AbstractNetworkClientManager networkClientManager = new DefaultNetworkClientManager();

        // Bundle it all together to form a Simple TCP Server
        final AbstractNetworkingServer tcpServer = new SimpleTcpServer(tcpServerConfig, networkClientManager, eventBus);

        // Add a Network Event Listener to the Event Bus - it will just print events to System.out
        networkClientEventListener(eventBus);

        // Let's add another listener to the Event Bus, for the NetworkMessageReceivedEvent, exposing a small protocol to the network
        eventBus.registerEventListener(NetworkMessageReceivedEvent.class, new AbstractEventListener<NetworkMessageReceivedEvent>()
        {
            @Override
            public void onEvent(NetworkMessageReceivedEvent event)
            {
                final String message = new String(event.getNetworkMessage().getMessageData());

                if ("Hello".equals(message))
                {
                    tcpServer.send(event.getNetworkClient(), "World".getBytes());
                }
                else if ("Bye".equals(message))
                {
                    tcpServer.stop(new Exception("Until next time!"), true);
                }
            }
        });

        // Let's go!
        tcpServer.start();
    }

    private static AbstractNetworkEventListener networkClientEventListener(AbstractEventBus eventBus)
    {
        return new AbstractNetworkEventListener(eventBus)
        {
            @Override
            public void onClientConnected(ClientConnectedEvent event)
            {
                event.print();
            }

            @Override
            public void onClientDisconnected(ClientDisconnectedEvent event)
            {
                event.print();
            }

            @Override
            public void onNetworkMessageReceived(NetworkMessageReceivedEvent event)
            {
                event.print();
            }

            @Override
            public void onNetworkMessageSent(NetworkMessageSentEvent event)
            {
                event.print();
            }

            @Override
            public void onServerReady(ServerReadyEvent event)
            {
                event.print();
            }

            @Override
            public void onServerClosed(ServerClosedEvent event)
            {
                event.print();
            }
        };
    }
}
