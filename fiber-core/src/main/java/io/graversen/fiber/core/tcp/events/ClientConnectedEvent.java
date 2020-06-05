package io.graversen.fiber.core.tcp.events;

import io.graversen.fiber.core.tcp.ITcpNetworkClient;
import io.graversen.fiber.event.BaseEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClientConnectedEvent extends BaseEvent {
    private final @NonNull ITcpNetworkClient networkClient;
}
