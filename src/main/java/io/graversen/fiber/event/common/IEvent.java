package io.graversen.fiber.event.common;

import java.time.LocalDateTime;

public interface IEvent
{
    default void print()
    {
        System.out.println(String.format("Event - %s", getClass().getSimpleName()));
    }

    LocalDateTime eventEmittedAt();

    LocalDateTime eventPropagatedAt();

    LocalDateTime eventFinishedExecutionAt();

    void propagate();

    void finish();
}
