package io.graversen.fiber.test.event;

import io.graversen.fiber.event.common.BaseEvent;
import io.graversen.fiber.event.common.IEvent;

public class PrintEvent extends BaseEvent implements IEvent
{
    private final int x;

    public PrintEvent(int x)
    {
        this.x = x;
    }

    public int getX()
    {
        return x;
    }
}
