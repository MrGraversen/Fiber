package io.graversen.fiber.core.tcp.events;

import io.graversen.fiber.core.tcp.ITcpNetworkClient;
import io.graversen.fiber.event.BaseEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class ClientDisconnectedEvent extends BaseEvent {
    private final @NonNull ITcpNetworkClient networkClient;
    private final @NonNull Throwable reason;
}
