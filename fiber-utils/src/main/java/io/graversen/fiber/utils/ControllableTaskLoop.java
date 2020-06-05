package io.graversen.fiber.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public abstract class ControllableTaskLoop<T> implements Runnable {
    private static final Object LOCK = new Object();
    private static final long LOCK_TIMEOUT = Duration.ofSeconds(1).toMillis();
    private volatile boolean running = true;

    @Override
    public void run() {
        Thread.currentThread().setName(threadName());
        while (!Thread.currentThread().isInterrupted()) {
            if (running) {
                try {
                    final var next = awaitNext();
                    performTask(next);
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
        running = false;
    }

    public void resume() {
        running = true;
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
