package io.graversen.fiber.core;

import lombok.NonNull;
import lombok.Value;

@Value
public class NetworkMessage {
    private final @NonNull byte[] message;
    private final @NonNull long bytes;
}
