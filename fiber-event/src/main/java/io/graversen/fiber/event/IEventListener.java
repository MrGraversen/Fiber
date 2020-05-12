package io.graversen.fiber.event;

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
            // Omitted
        }
    }

    void onEvent(T event);
}
