package io.graversen.fiber.event;

public interface IEvent
{
    default void print()
    {
        System.out.println(String.format("Event - %s", getClass().getSimpleName()));
    }
}
