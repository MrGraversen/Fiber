package io.graversen.fiber.event;

import java.time.LocalDateTime;

public class BaseEvent implements IEvent
{
    private final LocalDateTime eventTriggeredAt;
    private LocalDateTime eventPropagatedAt;
    private LocalDateTime eventFinishedExecutionAt;

    public BaseEvent()
    {
        this.eventTriggeredAt = LocalDateTime.now();
    }

    public LocalDateTime eventTriggeredAt()
    {
        return eventTriggeredAt;
    }

    public LocalDateTime eventPropagatedAt()
    {
        return eventPropagatedAt;
    }

    @Override
    public LocalDateTime eventFinishedExecutionAt()
    {
        return eventFinishedExecutionAt;
    }

    @Override
    public void propagate()
    {
        if (this.eventPropagatedAt == null)
        {
            this.eventPropagatedAt = LocalDateTime.now();
        }
        else
        {
            throw new RuntimeException("Event already propagated!");
        }
    }

    @Override
    public void finish()
    {
        if (this.eventFinishedExecutionAt == null)
        {
            this.eventFinishedExecutionAt = LocalDateTime.now();
        }
        else
        {
            throw new RuntimeException("Event already finished!");
        }
    }
}
