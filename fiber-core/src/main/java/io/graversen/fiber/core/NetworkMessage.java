package io.graversen.fiber.core;

import lombok.NonNull;
import lombok.Value;

import java.nio.charset.Charset;

@Value
public class NetworkMessage {
    private final @NonNull byte[] message;
    private final @NonNull long bytes;

    @Override
    public String toString() {
        return new String(message, Charset.defaultCharset());
    }
}
