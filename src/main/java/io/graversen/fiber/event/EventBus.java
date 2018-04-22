package io.graversen.fiber.event;

import io.graversen.fiber.server.async.DefaultThreadPool;
import io.graversen.fiber.util.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class EventBus
{
    private final ThreadPoolExecutor threadPool;
    private final Map<Class<? extends IEvent>, List<AbstractEventListener<? extends IEvent>>> eventListenerStore;
    private final Map<Class<? extends IEvent>, ConcurrentLinkedQueue<IEvent>> eventQueueStore;
    private final EventPropagator eventPropagator;

    public EventBus()
    {
        this.threadPool = new DefaultThreadPool(getThreadPoolSize(), getClass().getSimpleName());
        this.eventListenerStore = new ConcurrentHashMap<>();
        this.eventQueueStore = new ConcurrentHashMap<>();
        this.eventPropagator = new EventPropagator();
        this.threadPool.execute(eventPropagator);
    }

    public boolean hasEventListener(Class<? extends IEvent> eventClass)
    {
        final List<AbstractEventListener<? extends IEvent>> eventListeners = this.eventListenerStore.getOrDefault(eventClass, new ArrayList<>());
        return !eventListeners.isEmpty();
    }

    public void registerEventListener(Class<? extends IEvent> eventClass, AbstractEventListener<? extends IEvent> eventListener)
    {
        final List<AbstractEventListener<? extends IEvent>> eventListeners = eventListenerStore.getOrDefault(eventClass, new ArrayList<>());
        eventListeners.add(eventListener);

        this.eventListenerStore.put(eventClass, eventListeners);

        if (!this.eventQueueStore.containsKey(eventClass))
        {
            this.eventQueueStore.put(eventClass, new ConcurrentLinkedQueue<>());
        }
    }

    public void emitEvent(IEvent event)
    {
        this.emitEvent(event, false);
    }

    public void emitEvent(IEvent event, boolean requiresPropagation)
    {
        final List<AbstractEventListener<? extends IEvent>> eventListeners = this.eventListenerStore.getOrDefault(event.getClass(), new ArrayList<>());

        if (requiresPropagation && eventListeners.isEmpty())
        {
            throw new IllegalArgumentException(String.format("No event listener found for event %s. Did you register it?", event.getClass()));
        }

        final ConcurrentLinkedQueue<IEvent> eventQueue = eventQueueStore.get(event.getClass());
        eventQueue.add(event);

        eventQueueStore.put(event.getClass(), eventQueue);

        synchronized (eventPropagator.LOCK)
        {
            eventPropagator.LOCK.notify();
        }
    }

    public int getThreadPoolSize()
    {
        return Environment.availableProcessors();
    }

    private class EventPropagator implements Runnable
    {
        private final Object LOCK = new Object();
        private final int maxUniqueEventPropagation = 1000;

        @Override
        public void run()
        {
            try
            {
                while (!Thread.currentThread().isInterrupted())
                {
                    for (final Class<? extends IEvent> eventClass : eventListenerStore.keySet())
                    {
                        final ConcurrentLinkedQueue<IEvent> eventQueue = eventQueueStore.getOrDefault(eventClass, new ConcurrentLinkedQueue<>());

                        int eventsPropagated = 0;
                        while (eventQueue.peek() != null)
                        {
                            final IEvent event = eventQueue.poll();
                            eventListenerStore.get(eventClass).forEach(listener -> listener.propagateEvent(event));
                            if (maxUniqueEventPropagation <= ++eventsPropagated) break;
                        }
                    }

                    synchronized (LOCK)
                    {
                        LOCK.wait(1);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
