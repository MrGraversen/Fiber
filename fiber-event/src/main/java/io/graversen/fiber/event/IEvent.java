package io.graversen.fiber.event;

import java.time.Duration;
import java.time.LocalDateTime;

public interface IEvent {
    String id();

    LocalDateTime eventEmittedAt();

    LocalDateTime eventPropagatedAt();

    LocalDateTime eventHandledAt();

    void propagate();

    void finish();

    default Duration eventPropagationDelay() {
        if (eventPropagatedAt() == null) return Duration.ZERO;
        return Duration.between(eventEmittedAt(), eventPropagatedAt());
    }

    default Duration eventHandleDelay() {
        if (eventPropagatedAt() == null || eventHandledAt() == null) return Duration.ZERO;
        return Duration.between(eventPropagatedAt(), eventHandledAt());
    }

    default String eventName() {
        return getClass().getSimpleName();
    }
}
