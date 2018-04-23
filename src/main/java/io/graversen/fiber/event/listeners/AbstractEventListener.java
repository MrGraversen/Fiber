package io.graversen.fiber.event.listeners;

import io.graversen.fiber.event.common.IEvent;

public abstract class AbstractEventListener<T extends IEvent>
{
    public void propagateEvent(IEvent event)
    {
        try
        {
            onEvent((T) event);
        }
        catch (Exception ex1)
        {
            try
            {
                onEventError(ex1);
            }
            catch (Exception ex2)
            {
                ex2.printStackTrace();
            }
        }
    }

    public abstract void onEvent(T event);

    public void onEventError(Exception e)
    {
        e.printStackTrace();
    }
}
