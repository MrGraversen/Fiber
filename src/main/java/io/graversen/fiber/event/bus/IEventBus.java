package io.graversen.fiber.event.bus;

import io.graversen.fiber.event.listeners.AbstractEventListener;
import io.graversen.fiber.event.common.IEvent;

import java.util.concurrent.AbstractExecutorService;

public interface IEventBus
{
    boolean hasEventListener(Class<? extends IEvent> eventClass);

    void registerEventListener(Class<? extends IEvent> eventClass, AbstractEventListener<? extends IEvent> eventListener);

    void emitEvent(IEvent event);

    void emitEvent(IEvent event, boolean requiresPropagation);

    int getThreadPoolSize();
}
