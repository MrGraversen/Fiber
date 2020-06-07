package io.graversen.fiber.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public abstract class ControllableTaskLoop<T> implements Runnable {
    private static final Object LOCK = new Object();
    private static final long LOCK_TIMEOUT = Duration.ofSeconds(1).toMillis();
    private AtomicBoolean running = new AtomicBoolean(true);
    private AtomicBoolean stopping = new AtomicBoolean(false);

    @Override
    public void run() {
        Thread.currentThread().setName(threadName());
        while (!Thread.currentThread().isInterrupted() && !stopping.get()) {
            if (running.get()) {
                try {
                    final var next = awaitNext();
                    if (next != null) {
                        performTask(next);
                    }
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    } else {
                        taskFailed(e);
                    }
                }
            } else {
                synchronized (LOCK) {
                    try {
                        LOCK.wait(LOCK_TIMEOUT);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    public void pause() {
        running.set(false);
    }

    public void resume() {
        running.set(true);
    }

    public void stop() {
        stopping.set(true);
    }

    public void hint() {
        synchronized (LOCK) {
            LOCK.notify();
        }
    }

    public abstract void performTask(T nextItem);

    public void taskFailed(Exception exception) {
        log.error(exception.getMessage(), exception);
    }

    public abstract T awaitNext() throws InterruptedException;

    public String threadName() {
        return String.format("%s-%d", getClass().getSimpleName(), Thread.currentThread().getId());
    }
}
