package io.graversen.fiber.test.event;

import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.event.listeners.AbstractEventListener;
import io.graversen.fiber.event.bus.DefaultEventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.util.stream.IntStream;

class DefaultEventBusTest
{
    private IEventBus eventBus;

    @BeforeEach
    void setUp()
    {
        eventBus = new DefaultEventBus();
    }

    @Test
    @Disabled
    void testPerformance1() throws InterruptedException
    {
        final int eventCount = 10000000;
        final int eventSkipPrintCount = 1000;

        eventBus.registerEventListener(PrintEvent.class, new AbstractEventListener<PrintEvent>()
        {
            @Override
            public void onEvent(PrintEvent event)
            {
                final int x = event.getX();

                if (x % eventSkipPrintCount == 0)
                {
                    final String threadName = Thread.currentThread().getName();
                    System.out.println(String.format("PrintEvent (Thread: %s) (Emit: %d) (Propagated: %d): %d", threadName, event.eventEmittedAt().toEpochSecond(ZoneOffset.UTC), event.eventPropagatedAt().toEpochSecond(ZoneOffset.UTC), x));
                }
            }
        });

        eventBus.registerEventListener(IncrementEvent.class, new AbstractEventListener<IncrementEvent>()
        {
            @Override
            public void onEvent(IncrementEvent event)
            {
                final int x = event.getX();
                eventBus.emitEvent(new PrintEvent(x));
            }
        });

        IntStream.rangeClosed(1, eventCount).forEach(x -> eventBus.emitEvent(new IncrementEvent(x)));

        Thread.sleep(1000000);
    }
}