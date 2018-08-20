package io.graversen.fiber.event.common;

import java.time.LocalDateTime;

public abstract class BaseEvent implements IEvent
{
    private final boolean requiresPropagation;
    private final LocalDateTime eventEmittedAt;

    private LocalDateTime eventPropagatedAt;
    private LocalDateTime eventFinishedExecutionAt;

    public BaseEvent()
    {
        this(true);
    }

    public BaseEvent(boolean requiresPropagation)
    {
        this.requiresPropagation = requiresPropagation;
        this.eventEmittedAt = LocalDateTime.now();
    }

    public LocalDateTime eventEmittedAt()
    {
        return eventEmittedAt;
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
    public boolean requiresPropagation()
    {
        return requiresPropagation;
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
            throw new IllegalStateException(String.format("%s already propagated!", getClass().getSimpleName()));
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
            throw new IllegalStateException(String.format("%s already finished!", getClass().getSimpleName()));
        }
    }
}
