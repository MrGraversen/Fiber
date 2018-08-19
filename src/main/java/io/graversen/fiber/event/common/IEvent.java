package io.graversen.fiber.event.common;

import io.graversen.fiber.util.Constants;

import java.io.PrintStream;
import java.time.LocalDateTime;

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

    LocalDateTime eventEmittedAt();

    LocalDateTime eventPropagatedAt();

    LocalDateTime eventFinishedExecutionAt();

    boolean requiresPropagation();

    void propagate();

    void finish();
}
