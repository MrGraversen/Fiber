package io.graversen.fiber.event;

import java.time.LocalDateTime;

public interface IEvent
{
    default void print()
    {
        System.out.println(String.format("Event - %s", getClass().getSimpleName()));
    }

    LocalDateTime eventTriggeredAt();
}
