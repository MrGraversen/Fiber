package io.graversen.fiber.debug;

import io.graversen.fiber.event.IEvent;
import io.graversen.fiber.event.IEventListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingEventListener implements IEventListener<IEvent> {
    @Override
    public void onEvent(IEvent event) {
        log.info("\uD83D\uDCCB Handling event: {}: {}", event.getClass().getSimpleName(), event.toString());
    }
}
