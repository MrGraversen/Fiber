package io.graversen.fiber.event.bus;

import io.graversen.fiber.event.IEvent;
import io.graversen.fiber.event.IEventListener;
import io.graversen.fiber.utils.FiberEnvironment;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@Slf4j
public class DefaultEventBus implements IEventBus {
    private final Map<Class<? extends IEvent>, List<IEventListener<? extends IEvent>>> eventListenerStore;
    private final Map<Class<? extends IEvent>, ConcurrentLinkedQueue<IEvent>> eventQueueStore;
    private final Map<Integer, EventPropagator> eventPropagatorStore;
    private final AtomicInteger eventPropagatorRoundRobin;
    private final int cachedThreadPoolSize;

    private ThreadPoolExecutor threadPoolExecutor;

    private AtomicBoolean active = new AtomicBoolean(false);
    private AtomicBoolean pause = new AtomicBoolean(false);

    public DefaultEventBus() {
        this.cachedThreadPoolSize = getThreadPoolSize();
        this.eventListenerStore = new ConcurrentHashMap<>();
        this.eventQueueStore = new ConcurrentHashMap<>();
        this.eventPropagatorStore = new ConcurrentHashMap<>();
        this.eventPropagatorRoundRobin = new AtomicInteger(1);
    }

    @Override
    public boolean hasEventListener(Class<? extends IEvent> eventClass) {
        Objects.requireNonNull(eventClass, "Parameter 'eventClass' must not be null");
        final var eventListeners = eventListenerStore.computeIfAbsent(eventClass, e -> internalEventListenerList());
        return !eventListeners.isEmpty();
    }

    @Override
    public void registerEventListener(Class<? extends IEvent> eventClass, Supplier<IEventListener<? extends IEvent>> eventListener) {
        Objects.requireNonNull(eventClass, "Parameter 'eventClass' must not be null");
        registerEventListener(eventClass, eventListener.get());
    }

    @Override
    public void registerEventListener(Class<? extends IEvent> eventClass, IEventListener<? extends IEvent> eventListener) {
        Objects.requireNonNull(eventClass, "Parameter 'eventClass' must not be null");
        Objects.requireNonNull(eventListener, "Parameter 'eventListener' must not be null");

        final var eventListeners = eventListenerStore.computeIfAbsent(eventClass, e -> internalEventListenerList());
        eventListeners.add(eventListener);

        if (!this.eventQueueStore.containsKey(eventClass)) {
            this.eventQueueStore.put(eventClass, new ConcurrentLinkedQueue<>());
        }
    }

    @Override
    public void unregisterEventListeners(Class<? extends IEvent> eventClass) {
        Objects.requireNonNull(eventClass, "Parameter 'eventClass' must not be null");

        final var eventListeners = eventListenerStore.computeIfAbsent(eventClass, e -> internalEventListenerList());

        if (!eventListeners.isEmpty()) {
            eventListenerStore.remove(eventClass);
        }
    }

    @Override
    public void unregisterAllEventListeners() {
        eventListenerStore.keySet().forEach(this::unregisterEventListeners);
    }

    @Override
    public void emitEvent(IEvent event) {
        if (eventQueueStore.containsKey(event.getClass())) {
            final ConcurrentLinkedQueue<IEvent> eventQueue = eventQueueStore.get(event.getClass());
            eventQueue.add(event);

            eventQueueStore.put(event.getClass(), eventQueue);
            hintNextEventPropagator();
        } else {
            log.warn("Event listener not registered for event: {}", event.getClass());
        }
    }

    private void hintNextEventPropagator() {
        int propagator = eventPropagatorRoundRobin.incrementAndGet();
        if (propagator > cachedThreadPoolSize) propagator = 1;

        final EventPropagator nextEventPropagator = eventPropagatorStore.get(propagator);
        synchronized (nextEventPropagator.LOCK) {
            nextEventPropagator.LOCK.notify();
        }
    }

    @Override
    public void start() {
        if (active.compareAndSet(false, true)) {
            if (threadPoolExecutor != null) {
                threadPoolExecutor.shutdownNow();
                threadPoolExecutor = null;
            }

            this.threadPoolExecutor = new DefaultThreadPool(cachedThreadPoolSize, getClass().getSimpleName());
            this.threadPoolExecutor.prestartAllCoreThreads();

            IntStream.rangeClosed(1, cachedThreadPoolSize).forEach(i -> {
                final EventPropagator eventPropagator = new EventPropagator();
                eventPropagatorStore.put(i, eventPropagator);
                threadPoolExecutor.execute(eventPropagator);
            });
        }
    }

    @Override
    public void pause() {
        pause.set(true);
    }

    @Override
    public void resume() {
        pause.set(false);
    }

    @Override
    public void purgeAll() {
        if (threadPoolExecutor != null && active.get()) {
            final boolean pausedBefore = pause.get();
            if (!pausedBefore) pause();

            final BlockingQueue<Runnable> queue = threadPoolExecutor.getQueue();
            queue.forEach(threadPoolExecutor::remove);

            eventQueueStore.keySet().forEach(this::purge);

            if (!pausedBefore) resume();
        }
    }

    @Override
    public void purge(Class<? extends IEvent> eventType) {
        if (eventQueueStore.containsKey(eventType)) {
            eventQueueStore.get(eventType).clear();
        }
    }

    @Override
    public void stop(boolean gracefully) {
        if (active.compareAndSet(true, false)) {
            if (gracefully) {
                threadPoolExecutor.shutdownNow();
            } else {
                threadPoolExecutor.shutdown();
            }

            eventPropagatorStore.forEach((i, eventPropagator) -> {
                synchronized (eventPropagator.LOCK) {
                    eventPropagator.LOCK.notify();
                }
            });
        }
    }

    @Override
    public void stop() {
        this.stop(false);
    }

    public int getThreadPoolSize() {
        return FiberEnvironment.availableProcessors();
    }

    class EventPropagator implements Runnable {
        final Object LOCK = new Object();

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (!pause.get()) {
                        for (final Class<? extends IEvent> eventClass : eventListenerStore.keySet()) {
                            final var eventQueue = eventQueueStore.computeIfAbsent(eventClass, e -> new ConcurrentLinkedQueue<>());
                            final IEvent event = eventQueue.poll();

                            if (event != null) {
                                event.propagate();
                                eventListenerStore.get(eventClass).forEach(propagateEvent(event));
                                event.finish();
                            }
                        }
                    }

                    synchronized (LOCK) {
                        LOCK.wait(1000);
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        private Consumer<? super IEventListener<? extends IEvent>> propagateEvent(IEvent event) {
            return eventListener -> {
                try {
                    eventListener.propagate(event);
                } catch (Exception e) {
                    // Impossible to recover from; must be handled by concrete IEventListener instance
                }
            };
        }
    }

    private List<IEventListener<? extends IEvent>> internalEventListenerList() {
        return new CopyOnWriteArrayList<>();
    }
}
