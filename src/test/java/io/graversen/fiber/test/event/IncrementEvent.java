package io.graversen.fiber.test.event;

import io.graversen.fiber.event.BaseEvent;
import io.graversen.fiber.event.IEvent;

public class IncrementEvent extends BaseEvent implements IEvent
{
    private final int x;

    public IncrementEvent(int x)
    {
        this.x = x;
    }

    public int getX()
    {
        return x;
    }
}
