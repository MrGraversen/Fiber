package io.graversen.fiber.examples.multiclient;

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
import java.util.function.Predicate;

public class MultiClientExample {
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

        // 4. Greet all clients in an interval
        Tasks.interval(Duration.ofSeconds(3), () -> platform.server().broadcast("Hello, World!".getBytes()));

        // 5. Now we define our interactive protocol, where certain clients receive a special message
        // 5a. Define a query for clients that has the "magicKey" attribute
        final Predicate<ITcpNetworkClient> magicKeyQuery = client -> client.hasAttribute("magicKey", true);

        // 5b. In an interval, let the magic clients know they are special!
        Tasks.interval(Duration.ofSeconds(3), () -> platform.getClients(magicKeyQuery).send("You are a magic client!".getBytes()));

        // 5c. Whenever a clients sends "enchant" to us, it will receive the magic key, and sending "disenchant" will revoke it
        final IEventListener<NetworkReadEvent> magicKeyListener = event -> {
            final ITcpNetworkClient client = event.getNetworkClient();
            if ("enchant".equals(event.getNetworkMessage().toString())) {
                client.setAttribute("magicKey", true);
            } else if ("disenchant".equals(event.getNetworkMessage().toString())) {
                client.removeAttribute("magicKey");
            }
        };
        platform.eventBus().registerEventListener(NetworkReadEvent.class, magicKeyListener);

        // 6. After a little while, shut down the platform nicely
        Tasks.after(Duration.ofSeconds(30), platform::stop);
    }
}
