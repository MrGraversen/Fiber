package io.graversen.fiber.event.listeners;

import io.graversen.fiber.event.common.IEvent;

@FunctionalInterface
public interface IEventListener<T extends IEvent>
{
    default void propagate(IEvent event)
    {
        try
        {
            onEvent((T) event);
        }
        catch (Exception e)
        {
            onEventError(e);
        }
    }

    default void onEventError(Exception e)
    {
        e.printStackTrace();
    }

    void onEvent(T event);
}
