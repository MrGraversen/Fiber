package io.graversen.fiber.test.event;

import io.graversen.fiber.event.common.BaseEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EventTest
{
    @Test
    void test_eventObjectIntegrity()
    {
        final BaseEvent event = new TestEvent();
        assertNotNull(event.eventEmittedAt());
        assertNull(event.eventPropagatedAt());
        assertNull(event.eventFinishedExecutionAt());

        event.propagate();
        assertNotNull(event.eventEmittedAt());
        assertNotNull(event.eventPropagatedAt());
        assertNull(event.eventFinishedExecutionAt());

        event.finish();
        assertNotNull(event.eventEmittedAt());
        assertNotNull(event.eventPropagatedAt());
        assertNotNull(event.eventFinishedExecutionAt());
    }

    @Test
    void test_noDoublePropagate()
    {
        final BaseEvent event = new TestEvent();
        event.propagate();

        assertThrows(IllegalStateException.class, event::propagate);
    }

    @Test
    void test_noDoubleFinish()
    {
        final BaseEvent event = new TestEvent();
        event.propagate();
        event.finish();

        assertThrows(IllegalStateException.class, event::finish);
    }

    private class TestEvent extends BaseEvent
    {
        // A whole lot of nothing!
    }
}
