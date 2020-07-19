package io.graversen.fiber.debug;

import io.graversen.fiber.core.tcp.AsyncEventDrivenTcpPlatform;
import io.graversen.fiber.core.tcp.ServerNetworkConfiguration;
import io.graversen.fiber.core.tcp.events.*;
import io.graversen.fiber.event.IEvent;
import io.graversen.fiber.event.IEventListener;
import io.graversen.fiber.event.bus.IEventBus;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DebugTcpPlatform extends AsyncEventDrivenTcpPlatform {
    private static final IEventListener<IEvent> LOGGING_EVENT_LISTENER = new LoggingEventListener();
    private static final List<Class<? extends IEvent>> LOGGING_EVENTS = List.of(
            ClientConnectedEvent.class,
            ClientDisconnectedEvent.class,
            NetworkReadEvent.class,
            NetworkWriteEvent.class,
            ServerStartedEvent.class,
            ServerStoppedEvent.class
    );

    public DebugTcpPlatform(IEventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void start(ServerNetworkConfiguration networkConfiguration) {
        final var eventNames = LOGGING_EVENTS.stream().map(Class::getSimpleName).collect(Collectors.toUnmodifiableList());
        log.info("Registering logging listeners for: {}", String.join(", ", eventNames));
        LOGGING_EVENTS.forEach(eventClass -> eventBus().registerEventListener(eventClass, LOGGING_EVENT_LISTENER));
        super.start(networkConfiguration);
    }
}
