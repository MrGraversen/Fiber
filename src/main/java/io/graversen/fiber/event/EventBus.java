package io.graversen.fiber.event;

import org.ownzone.lib.network.server.async.DefaultThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public final class EventBus
{
    private final int THREAD_POOL_SIZE = 1;

    private final ScheduledThreadPoolExecutor threadPool;
    private final Map<Class<? extends IEvent>, List<AbstractEventListener<? extends IEvent>>> eventListenerStore;
    private final Map<Class<? extends IEvent>, ConcurrentLinkedQueue<IEvent>> eventQueueStore;
    private final EventPropagator eventPropagator;

    public EventBus()
    {
        this.threadPool = new DefaultThreadPool(THREAD_POOL_SIZE, getClass().getSimpleName());
        this.eventListenerStore = new ConcurrentHashMap<>();
        this.eventQueueStore = new ConcurrentHashMap<>();
        this.eventPropagator = new EventPropagator();
        this.threadPool.execute(eventPropagator);
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

    public void publishEvent(IEvent event)
    {
        final List<AbstractEventListener<? extends IEvent>> eventListener = this.eventListenerStore.getOrDefault(event.getClass(), new ArrayList<>());

        if (eventListener.isEmpty())
        {
            throw new IllegalArgumentException(String.format("No event listener found for event %s. Did you register it?", event.getClass()));
        }

        final ConcurrentLinkedQueue<IEvent> eventQueue = eventQueueStore.get(event.getClass());
        eventQueue.add(event);

        eventQueueStore.put(event.getClass(), eventQueue);
    }

    private class EventPropagator implements Runnable
    {
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

                        if (eventQueue.peek() != null)
                        {
                            final IEvent event = eventQueue.poll();
                            event.print();

                            eventListenerStore.get(eventClass).forEach(listener -> listener.propagateEvent(event));
                        }
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
