package io.graversen.fiber.event.bus;

import io.graversen.fiber.event.common.IEvent;
import io.graversen.fiber.event.listeners.BaseEventListener;
import io.graversen.fiber.server.async.DefaultThreadPool;
import io.graversen.fiber.util.Constants;
import io.graversen.fiber.util.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class DefaultEventBus implements IEventBus
{
    private final ThreadPoolExecutor threadPoolExecutor;
    private final Map<Class<? extends IEvent>, List<BaseEventListener<? extends IEvent>>> eventListenerStore;
    private final Map<Class<? extends IEvent>, ConcurrentLinkedQueue<IEvent>> eventQueueStore;
    private final Map<Integer, EventPropagator> eventPropagatorStore;
    private final AtomicInteger eventPropagatorRoundRobin;
    private final int threadPoolSize;

    private boolean active = false;
    private volatile boolean pause;

    public DefaultEventBus()
    {
        this.threadPoolSize = getThreadPoolSize();
        this.threadPoolExecutor = new DefaultThreadPool(threadPoolSize, getClass().getSimpleName());
        this.eventListenerStore = new ConcurrentHashMap<>();
        this.eventQueueStore = new ConcurrentHashMap<>();
        this.eventPropagatorStore = new ConcurrentHashMap<>();
        this.eventPropagatorRoundRobin = new AtomicInteger(1);
    }

    @Override
    public boolean hasEventListener(Class<? extends IEvent> eventClass)
    {
        final List<BaseEventListener<? extends IEvent>> eventListeners = this.eventListenerStore.getOrDefault(eventClass, new ArrayList<>());
        return !eventListeners.isEmpty();
    }

    @Override
    public void registerEventListener(Class<? extends IEvent> eventClass, BaseEventListener<? extends IEvent> eventListener)
    {
        final List<BaseEventListener<? extends IEvent>> eventListeners = eventListenerStore.getOrDefault(eventClass, new ArrayList<>());
        eventListeners.add(eventListener);

        this.eventListenerStore.put(eventClass, eventListeners);

        if (!this.eventQueueStore.containsKey(eventClass))
        {
            this.eventQueueStore.put(eventClass, new ConcurrentLinkedQueue<>());
        }
    }

    @Override
    public void emitEvent(IEvent event)
    {
        this.emitEvent(event, false);
    }

    @Override
    public void emitEvent(IEvent event, boolean requiresPropagation)
    {
        final List<BaseEventListener<? extends IEvent>> eventListeners = this.eventListenerStore.getOrDefault(event.getClass(), new ArrayList<>());

        if ((event.requiresPropagation() || requiresPropagation) && eventListeners.isEmpty())
        {
            throw new IllegalArgumentException(String.format("No event listener found for event %s. Did you register it?", event.getClass()));
        }

        final ConcurrentLinkedQueue<IEvent> eventQueue = eventQueueStore.get(event.getClass());
        eventQueue.add(event);

        eventQueueStore.put(event.getClass(), eventQueue);
        provokeNextEventPropagator();
    }

    private void provokeNextEventPropagator()
    {
        int propagator = eventPropagatorRoundRobin.incrementAndGet();
        if (propagator > threadPoolSize) propagator = 1;

        final EventPropagator nextEventPropagator = eventPropagatorStore.get(propagator);
        synchronized (nextEventPropagator.LOCK)
        {
            nextEventPropagator.LOCK.notify();
        }
    }

    @Override
    public int getThreadPoolSize()
    {
        return Environment.availableProcessors();
    }

    @Override
    public void start()
    {
        if (!active)
        {
            IntStream.rangeClosed(1, threadPoolSize).forEach(i ->
            {
                final EventPropagator eventPropagator = new EventPropagator();
                eventPropagatorStore.put(i, eventPropagator);
                threadPoolExecutor.execute(eventPropagator);
            });

            active = true;
        }
    }

    @Override
    public void pause()
    {
        pause = true;
    }

    @Override
    public void resume()
    {
        pause = false;
    }

    @Override
    public void stop()
    {
        threadPoolExecutor.shutdown();
        active = false;
    }

    private class EventPropagator implements Runnable
    {
        final Object LOCK = new Object();

        @Override
        public void run()
        {
            try
            {
                while (!Thread.currentThread().isInterrupted())
                {
                    if (!pause)
                    {
                        for (final Class<? extends IEvent> eventClass : eventListenerStore.keySet())
                        {
                            final ConcurrentLinkedQueue<IEvent> eventQueue = eventQueueStore.getOrDefault(eventClass, new ConcurrentLinkedQueue<>());

                            int eventsPropagated = 0;
                            while (eventQueue.peek() != null)
                            {
                                final IEvent event = eventQueue.poll();

                                if (event != null)
                                {
                                    event.propagate();
                                    eventListenerStore.get(eventClass).forEach(listener -> listener.propagateEvent(event));
                                    event.finish();
                                }

                                if (Constants.MAX_UNIQUE_SEQUENTIAL_EVENTS <= ++eventsPropagated) break;
                            }
                        }
                    }

                    synchronized (LOCK)
                    {
                        LOCK.wait(Constants.CONCURRENCY_LOCK_EXPIRY_MILLIS);
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
