package io.graversen.fiber.event.bus;

import io.graversen.fiber.event.IEvent;
import io.graversen.fiber.event.IEventListener;

import java.util.function.Supplier;

public interface IEventBus {
    boolean hasEventListener(Class<? extends IEvent> eventClass);

    void registerEventListener(Class<? extends IEvent> eventClass, Supplier<IEventListener<? extends IEvent>> eventListener);

    void registerEventListener(Class<? extends IEvent> eventClass, IEventListener<? extends IEvent> eventListener);

    void unregisterEventListeners(Class<? extends IEvent> eventClass);

    void unregisterAllEventListeners();

    void emitEvent(IEvent event);

    void start();

    void pause();

    void resume();

    void purgeAll();

    void purge(Class<? extends IEvent> eventType);

    void stop(boolean gracefully);

    void stop();
}
