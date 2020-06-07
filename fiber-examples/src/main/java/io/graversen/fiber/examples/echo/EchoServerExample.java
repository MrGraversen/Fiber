package io.graversen.fiber.examples.echo;

import io.graversen.fiber.core.IPlatform;
import io.graversen.fiber.core.tcp.AsyncEventDrivenTcpPlatform;
import io.graversen.fiber.core.tcp.ITcpNetworkClient;
import io.graversen.fiber.core.tcp.ServerNetworkConfiguration;
import io.graversen.fiber.core.tcp.events.NetworkReadEvent;
import io.graversen.fiber.event.IEventListener;
import io.graversen.fiber.event.bus.DefaultEventBus;
import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.utils.Tasks;

import java.time.Duration;

public class EchoServerExample {
    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    }

    public static void main(String[] args) {
        // 1. We must first specify an EventBus
        final IEventBus eventBus = new DefaultEventBus();

        // 2. Instantiate the AsyncEventDrivenTcpPlatform
        final IPlatform<ITcpNetworkClient> platform = new AsyncEventDrivenTcpPlatform(eventBus);

        // 3. Let's go! In this example, we bind the server to port 1337
        platform.start(ServerNetworkConfiguration.localhost(1337));

        // 4. Implement the echo "protocol" - whichever data we receive will be written back to the client
        final IEventListener<NetworkReadEvent> echoingListener = event -> {
            final ITcpNetworkClient client = event.getNetworkClient();
            final byte[] message = event.getNetworkMessage().getMessage();
            platform.server().send(client, message);
        };
        platform.eventBus().registerEventListener(NetworkReadEvent.class, echoingListener);

        // 5. After a little while, shut down the platform nicely
        Tasks.after(Duration.ofSeconds(30), platform::stop);
    }
}
