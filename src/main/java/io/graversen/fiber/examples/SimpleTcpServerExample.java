package io.graversen.fiber.examples;

import io.graversen.fiber.config.tcp.AllNetworkInterfacesTcpServerConfig;
import io.graversen.fiber.config.tcp.TcpServerConfig;
import io.graversen.fiber.event.bus.DefaultEventBus;
import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.event.common.*;
import io.graversen.fiber.event.listeners.BaseNetworkEventListener;
import io.graversen.fiber.event.listeners.IEventListener;
import io.graversen.fiber.event.listeners.reference.PrintingEventListener;
import io.graversen.fiber.server.base.BaseNetworkingServer;
import io.graversen.fiber.server.management.BaseNetworkClientManager;
import io.graversen.fiber.server.management.DefaultNetworkClientManager;
import io.graversen.fiber.server.tcp.SimpleTcpServer;

public class SimpleTcpServerExample
{
    // First, let's configure the TCP server instance - will listen on port 1337
    static final TcpServerConfig tcpServerConfig = new AllNetworkInterfacesTcpServerConfig(1337);

    // Declare an implementation of the Event Bus
    static final IEventBus eventBus = new DefaultEventBus();

    // Declare an implementation of Network Client Manager
    static final BaseNetworkClientManager networkClientManager = new DefaultNetworkClientManager();

    // Bundle it all together to form a Simple TCP Server
    static final BaseNetworkingServer tcpServer = new SimpleTcpServer(tcpServerConfig, networkClientManager, eventBus);

    public static void main(String[] args)
    {
        // Add the reference PrintingEventListener - it will just print events to System.out
        final PrintingEventListener printingEventListener = new PrintingEventListener(eventBus);

        // Let's add another listener to the Event Bus, for the NetworkMessageReceivedEvent, exposing a small protocol to the network
        eventBus.registerEventListener(NetworkMessageReceivedEvent.class, SimpleTcpServerExample::networkListener);

        // Let's go!
        tcpServer.start();
    }

    private static IEventListener<NetworkMessageReceivedEvent> networkListener()
    {
        return event ->
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
        };
    }
}
