package io.graversen.fiber.test.event;

import io.graversen.fiber.event.bus.AbstractEventBus;
import io.graversen.fiber.event.listeners.AbstractEventListener;
import io.graversen.fiber.event.bus.DefaultEventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.util.stream.IntStream;

class DefaultEventBusTest
{
    private AbstractEventBus abstractEventBus;

    @BeforeEach
    void setUp()
    {
        abstractEventBus = new DefaultEventBus();
    }

    @Test
    @Disabled
    void testPerformance1() throws InterruptedException
    {
        final int eventCount = 10000000;
        final int eventSkipPrintCount = 1000;

        abstractEventBus.registerEventListener(PrintEvent.class, new AbstractEventListener<PrintEvent>()
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

        abstractEventBus.registerEventListener(IncrementEvent.class, new AbstractEventListener<IncrementEvent>()
        {
            @Override
            public void onEvent(IncrementEvent event)
            {
                final int x = event.getX();
                abstractEventBus.emitEvent(new PrintEvent(x));
            }
        });

        IntStream.rangeClosed(1, eventCount).forEach(x -> abstractEventBus.emitEvent(new IncrementEvent(x)));

        Thread.sleep(1000000);
    }
}