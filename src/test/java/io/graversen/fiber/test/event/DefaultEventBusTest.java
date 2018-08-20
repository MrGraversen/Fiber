package io.graversen.fiber.test.event;

import io.graversen.fiber.event.bus.DefaultEventBus;
import io.graversen.fiber.event.common.BaseEvent;
import io.graversen.fiber.event.common.IEvent;
import io.graversen.fiber.event.listeners.IEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class DefaultEventBusTest
{
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private DefaultEventBus eventBus;

    @BeforeEach
    void setUp()
    {
        eventBus = new DefaultEventBus();
        eventBus.start();
    }

    @Test
    void test_registerEventListener()
    {
        eventBus.registerEventListener(TestEvent.class, new TestEventListener());
        assertTrue(eventBus.hasEventListener(TestEvent.class));
    }

    @Test
    void test_requiresPropagationFailure()
    {
        assertThrows(IllegalArgumentException.class, () -> eventBus.emitEvent(new TestEvent(), true));
    }

    @Test
    @Disabled
    void testPerformance1() throws InterruptedException
    {
        final int eventCount = 1_000_000;
        final int eventSkipPrintCount = 100;

        eventBus.registerEventListener(PrintEvent.class, (IEventListener<PrintEvent>) event ->
        {
            final int x = event.getX();

            if (x % eventSkipPrintCount == 0)
            {
                final String threadName = Thread.currentThread().getName();
                System.out.println(String.format("PrintEvent (Thread: %s) (Propagate Delay: %d) (Execute Duration: %d) \t x = %d", threadName, event.eventPropagationDelay(), event.eventExecutionDuration(), event.getX()));
            }
        });

        eventBus.registerEventListener(IncrementEvent.class, (IEventListener<IncrementEvent>) event ->
        {
            final int x = event.getX();
            eventBus.emitEvent(new PrintEvent(x));

            if (x % eventSkipPrintCount == 0)
            {
                final String threadName = Thread.currentThread().getName();
                System.out.println(String.format("IncrementEvent (Thread: %s) (Propagate Delay: %d) (Execute Duration: %d) \t x = %d", threadName, event.eventPropagationDelay(), event.eventExecutionDuration(), event.getX()));
            }
        });

        IntStream.rangeClosed(1, eventCount).forEach(x -> eventBus.emitEvent(new IncrementEvent(x)));

        Thread.sleep(1000000);
    }

    private class TestEvent extends BaseEvent
    {

    }

    private class TestEventListener implements IEventListener<TestEvent>
    {
        @Override
        public void onEvent(TestEvent event)
        {

        }
    }
}