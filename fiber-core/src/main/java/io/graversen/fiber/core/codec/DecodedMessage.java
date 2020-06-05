package io.graversen.fiber.core.codec;

import io.graversen.fiber.core.tcp.ITcpNetworkClient;
import lombok.NonNull;
import lombok.Value;

@Value
public class DecodedMessage<T> {
    private final @NonNull T value;
    private final @NonNull ITcpNetworkClient client;
}
