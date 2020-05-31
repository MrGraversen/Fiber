package io.graversen.fiber.core.tcp.events;

import io.graversen.fiber.core.IServer;
import io.graversen.fiber.event.BaseEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ServerStoppedEvent extends BaseEvent {
    private final @NonNull IServer<?> server;
    private final @NonNull Throwable reason;
}
