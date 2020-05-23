package io.graversen.fiber.event;

public interface IErrorHandlingEventListener<T extends IEvent> extends IEventListener<T> {
    @Override
    default void propagate(IEvent event) {
        try {
            onEvent((T) event);
        } catch (Exception e) {
            onEventError(e);
        }
    }

    void onEventError(Throwable reason);
}
