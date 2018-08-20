package io.graversen.fiber.event.bus;

import io.graversen.fiber.event.common.IEvent;
import io.graversen.fiber.event.listeners.IEventListener;

import java.util.function.Supplier;

public interface IEventBus
{
    boolean hasEventListener(Class<? extends IEvent> eventClass);

    void registerEventListener(Class<? extends IEvent> eventClass, Supplier<IEventListener<? extends IEvent>> eventListener);

    void registerEventListener(Class<? extends IEvent> eventClass, IEventListener<? extends IEvent> eventListener);

    void emitEvent(IEvent event);

    void emitEvent(IEvent event, boolean requiresPropagation);

    int getThreadPoolSize();

    void start();

    void pause();

    void resume();

    void stop();
}
