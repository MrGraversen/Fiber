package io.graversen.fiber.core;

import io.graversen.fiber.utils.IClient;
import lombok.Getter;
import lombok.NonNull;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class NetworkPayload {
    private final @NonNull ByteBuffer byteBuffer;
    private final @NonNull IClient client;
    private final AtomicInteger requeueCount = new AtomicInteger(0);

    public NetworkPayload(@NonNull ByteBuffer byteBuffer, @NonNull IClient client) {
        this.byteBuffer = byteBuffer.asReadOnlyBuffer();
        this.client = client;
    }

    public void registerRequeue() {
        requeueCount.incrementAndGet();
    }
}
