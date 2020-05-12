package io.graversen.fiber.event;

import io.graversen.fiber.utils.IdUtils;

import java.time.LocalDateTime;

public abstract class BaseEvent implements IEvent {
    private final String id;
    private final LocalDateTime eventEmittedAt;

    private LocalDateTime eventPropagatedAt;
    private LocalDateTime eventHandledAt;

    protected BaseEvent(String id) {
        this.id = id;
        this.eventEmittedAt = LocalDateTime.now();
    }

    protected BaseEvent() {
        this(IdUtils.fastEventId());
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public LocalDateTime eventEmittedAt() {
        return eventEmittedAt;
    }

    @Override
    public LocalDateTime eventPropagatedAt() {
        return eventPropagatedAt;
    }

    @Override
    public LocalDateTime eventHandledAt() {
        return eventHandledAt;
    }

    @Override
    public void propagate() {
        if (this.eventPropagatedAt == null) {
            this.eventPropagatedAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException(String.format("%s already propagated!", getClass().getSimpleName()));
        }
    }

    @Override
    public void finish() {
        if (this.eventHandledAt == null) {
            this.eventHandledAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException(String.format("%s already finished!", getClass().getSimpleName()));
        }
    }

    public static EventSignature signature() {
        return new EventSignature(BaseEvent.class.getEnclosingClass());
    }
}
