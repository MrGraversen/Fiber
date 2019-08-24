package io.graversen.fiber.event.bus;

import io.graversen.fiber.event.common.IEvent;
import io.graversen.fiber.event.listeners.IEventListener;
import io.graversen.fiber.server.async.DefaultThreadPool;
import io.graversen.fiber.util.Constants;
import io.graversen.fiber.util.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class DefaultEventBus implements IEventBus
{
    private final Map<Class<? extends IEvent>, List<IEventListener<? extends IEvent>>> eventListenerStore;
    private final Map<Class<? extends IEvent>, ConcurrentLinkedQueue<IEvent>> eventQueueStore;
    private final Map<Integer, EventPropagator> eventPropagatorStore;
    private final AtomicInteger eventPropagatorRoundRobin;
    private final int cachedThreadPoolSize;

    private ThreadPoolExecutor threadPoolExecutor;

    private volatile boolean active = false;
    private volatile boolean pause;

    public DefaultEventBus()
    {
        this.cachedThreadPoolSize = getThreadPoolSize();
        this.eventListenerStore = new ConcurrentHashMap<>();
        this.eventQueueStore = new ConcurrentHashMap<>();
        this.eventPropagatorStore = new ConcurrentHashMap<>();
        this.eventPropagatorRoundRobin = new AtomicInteger(1);
    }

    @Override
    public boolean hasEventListener(Class<? extends IEvent> eventClass)
    {
        Objects.requireNonNull(eventClass, "Parameter 'eventClass' must not be null");
        final List<IEventListener<? extends IEvent>> eventListeners = this.eventListenerStore.getOrDefault(eventClass, internalEventListenerList());
        return !eventListeners.isEmpty();
    }

    @Override
    public void registerEventListener(Class<? extends IEvent> eventClass, Supplier<IEventListener<? extends IEvent>> eventListener)
    {
        Objects.requireNonNull(eventClass, "Parameter 'eventClass' must not be null");
        registerEventListener(eventClass, eventListener.get());
    }

    @Override
    public void registerEventListener(Class<? extends IEvent> eventClass, IEventListener<? extends IEvent> eventListener)
    {
        Objects.requireNonNull(eventClass, "Parameter 'eventClass' must not be null");
        Objects.requireNonNull(eventListener, "Parameter 'eventListener' must not be null");

        final List<IEventListener<? extends IEvent>> eventListeners = eventListenerStore.getOrDefault(eventClass, internalEventListenerList());
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
        this.emitEvent(event, event.requiresPropagation());
    }

    @Override
    public void emitEvent(IEvent event, boolean requiresPropagation)
    {
        final List<IEventListener<? extends IEvent>> eventListeners = this.eventListenerStore.getOrDefault(event.getClass(), internalEventListenerList());

        if (requiresPropagation && eventListeners.isEmpty())
        {
            throw new IllegalArgumentException(String.format("No event listener found for event %s. Did you register it?", event.getClass()));
        }

        if (eventQueueStore.containsKey(event.getClass()))
        {
            final ConcurrentLinkedQueue<IEvent> eventQueue = eventQueueStore.get(event.getClass());
            eventQueue.add(event);

            eventQueueStore.put(event.getClass(), eventQueue);
            provokeNextEventPropagator();
        }
    }

    private void provokeNextEventPropagator()
    {
        int propagator = eventPropagatorRoundRobin.incrementAndGet();
        if (propagator > cachedThreadPoolSize) propagator = 1;

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
            if (threadPoolExecutor != null)
            {
                threadPoolExecutor.shutdownNow();
                threadPoolExecutor = null;
            }

            this.threadPoolExecutor = new DefaultThreadPool(cachedThreadPoolSize, getClass().getSimpleName());
            this.threadPoolExecutor.prestartAllCoreThreads();

            IntStream.rangeClosed(1, cachedThreadPoolSize).forEach(i ->
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
    public void purgeAll()
    {
        if (threadPoolExecutor != null && active)
        {
            final boolean pausedBefore = pause;
            if (!pausedBefore) pause();

            final BlockingQueue<Runnable> queue = threadPoolExecutor.getQueue();
            queue.forEach(threadPoolExecutor::remove);

            eventQueueStore.keySet().forEach(this::purge);

            if (!pausedBefore) resume();
        }
    }

    @Override
    public void purge(Class<? extends IEvent> eventType)
    {
        if (eventQueueStore.containsKey(eventType))
        {
            eventQueueStore.get(eventType).clear();
        }
    }

    @Override
    public void stop(boolean gracefully)
    {
        if (active)
        {
            if (gracefully)
            {
                threadPoolExecutor.shutdownNow();
            }
            else
            {
                threadPoolExecutor.shutdown();
            }

            active = false;

            eventPropagatorStore.forEach((i, eventPropagator) ->
            {
                synchronized (eventPropagator.LOCK)
                {
                    eventPropagator.LOCK.notify();
                }
            });
        }
    }

    @Override
    public void stop()
    {
        this.stop(false);
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
                                    eventListenerStore.get(eventClass).forEach(propagateEvent(event));
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
                // Ignore
            }
        }

        private Consumer<? super IEventListener<? extends IEvent>> propagateEvent(IEvent event)
        {
            return eventListener ->
            {
                try
                {
                    eventListener.propagate(event);
                }
                catch (Exception e)
                {
                    // Impossible to recover from; must be handled by concrete IEventListener
                }
            };
        }
    }

    private List<IEventListener<? extends IEvent>> internalEventListenerList()
    {
        return new CopyOnWriteArrayList<>();
    }
}
