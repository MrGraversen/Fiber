package io.graversen.fiber.utils;

import java.time.Duration;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ControllableTaskLoop implements Runnable {
    private final ReentrantLock lock;
    private final Condition hasAdditionalTasks;
    private final long lockTimeoutMillis;

    private volatile boolean running = true;

    public ControllableTaskLoop(Duration lockTimeout) {
        this.lock = new ReentrantLock();
        this.hasAdditionalTasks = lock.newCondition();
        this.lockTimeoutMillis = lockTimeout.toMillis();
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (running) {
                    try {
                        performTask();
                    } catch (Exception e) {
                        taskFailed(e);
                    }
                }

                synchronized (lock) {
                    lock.wait(lockTimeoutMillis);
                }
            }
        } catch (Exception e) {
            // Ignore
        }

    }

    public void pause() {
        running = false;
    }

    public void resume() {
        running = true;
    }

    protected Object getLock() {
        return lock;
    }

    public abstract void performTask();

    public abstract void taskFailed(Exception exception);
}
