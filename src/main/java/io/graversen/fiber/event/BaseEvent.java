package io.graversen.fiber.event;

import java.time.LocalDateTime;

public class BaseEvent implements IEvent
{
    private final LocalDateTime eventTriggeredAt;

    public BaseEvent()
    {
        this.eventTriggeredAt = LocalDateTime.now();
    }

    public LocalDateTime eventTriggeredAt()
    {
        return eventTriggeredAt;
    }
}
