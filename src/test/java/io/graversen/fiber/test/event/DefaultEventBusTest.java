package io.graversen.fiber.test.event;

import io.graversen.fiber.event.bus.DefaultEventBus;
import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.event.listeners.BaseEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

class DefaultEventBusTest
{
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private IEventBus eventBus;

    @BeforeEach
    void setUp()
    {
        eventBus = new DefaultEventBus();
        eventBus.start();
    }

    @Test
    @Disabled
    void testPerformance1() throws InterruptedException
    {
        final int eventCount = 1_000_000;
        final int eventSkipPrintCount = 100;

        eventBus.registerEventListener(PrintEvent.class, new BaseEventListener<PrintEvent>()
        {
            @Override
            public void onEvent(PrintEvent event)
            {
                final int x = event.getX();

                if (x % eventSkipPrintCount == 0)
                {
                    final String threadName = Thread.currentThread().getName();
                    System.out.println(String.format("PrintEvent (Thread: %s) (Propagate Delay: %d) (Execute Duration: %d) \t x = %d", threadName, event.eventPropagationDelay(), event.eventExecutionDuration(), event.getX()));
//                    System.out.println(String.format("PrintEvent (Thread: %s) (Emit: %s) (Propagated: %s): %d", threadName, event.eventEmittedAt().format(dateTimeFormatter), event.eventPropagatedAt().format(dateTimeFormatter), x));
                }
            }
        });

        eventBus.registerEventListener(IncrementEvent.class, new BaseEventListener<IncrementEvent>()
        {
            @Override
            public void onEvent(IncrementEvent event)
            {
                final int x = event.getX();
                eventBus.emitEvent(new PrintEvent(x));

                if (x % eventSkipPrintCount == 0)
                {
                    final String threadName = Thread.currentThread().getName();
                    System.out.println(String.format("IncrementEvent (Thread: %s) (Propagate Delay: %d) (Execute Duration: %d) \t x = %d", threadName, event.eventPropagationDelay(), event.eventExecutionDuration(), event.getX()));
//                    System.out.println(String.format("IncrementEvent (Thread: %s) (Emit: %s) (Propagated: %s): %d", threadName, event.eventEmittedAt().format(dateTimeFormatter), event.eventPropagatedAt().format(dateTimeFormatter), x));
                }
            }
        });

        IntStream.rangeClosed(1, eventCount).forEach(x -> eventBus.emitEvent(new IncrementEvent(x)));

        Thread.sleep(1000000);
    }
}