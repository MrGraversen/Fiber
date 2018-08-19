package io.graversen.fiber.event.common;

import io.graversen.fiber.util.Constants;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public interface IEvent
{
    default void print()
    {
        print(System.out);
    }

    default void print(PrintStream printStream)
    {
        printStream.printf("[%s]: Event: %s", Constants.PROJECT_NAME, getClass().getSimpleName());
    }

    default long eventPropagationDelay()
    {
        if (eventPropagatedAt() == null) return 0L;
        return ChronoUnit.MILLIS.between(eventEmittedAt(), eventPropagatedAt());
    }

    default long eventExecutionDuration()
    {
        if (eventPropagatedAt() == null || eventFinishedExecutionAt() == null) return 0L;
        return ChronoUnit.MILLIS.between(eventPropagatedAt(), eventFinishedExecutionAt());
    }

    LocalDateTime eventEmittedAt();

    LocalDateTime eventPropagatedAt();

    LocalDateTime eventFinishedExecutionAt();

    boolean requiresPropagation();

    void propagate();

    void finish();
}
