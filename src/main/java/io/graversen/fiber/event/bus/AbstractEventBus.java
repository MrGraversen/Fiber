package io.graversen.fiber.event.bus;

import io.graversen.fiber.event.listeners.AbstractEventListener;
import io.graversen.fiber.event.common.IEvent;

import java.util.concurrent.AbstractExecutorService;

public abstract class AbstractEventBus
{
    protected AbstractExecutorService executorService;

    public abstract boolean hasEventListener(Class<? extends IEvent> eventClass);

    public abstract void registerEventListener(Class<? extends IEvent> eventClass, AbstractEventListener<? extends IEvent> eventListener);

    public abstract void emitEvent(IEvent event);

    public abstract void emitEvent(IEvent event, boolean requiresPropagation);

    public abstract int getThreadPoolSize();
}
