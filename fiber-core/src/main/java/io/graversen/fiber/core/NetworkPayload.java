package io.graversen.fiber.core;

import io.graversen.fiber.utils.IClient;
import lombok.NonNull;
import lombok.Value;

import java.nio.ByteBuffer;

@Value
public class NetworkPayload {
    private final @NonNull ByteBuffer byteBuffer;
    private final @NonNull IClient client;
}
