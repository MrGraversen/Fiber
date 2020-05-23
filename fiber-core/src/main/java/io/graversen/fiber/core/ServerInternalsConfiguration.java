package io.graversen.fiber.core;

import lombok.NonNull;
import lombok.Value;

@Value
public class ServerInternalsConfiguration {
    private final @NonNull int bufferSizeBytes;
    private final @NonNull int maximumNetworkRequeue;
}
