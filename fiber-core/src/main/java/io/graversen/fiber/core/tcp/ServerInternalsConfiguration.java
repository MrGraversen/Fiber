package io.graversen.fiber.core.tcp;

import lombok.NonNull;
import lombok.Value;

@Value
public class ServerInternalsConfiguration {
    private final @NonNull int bufferSizeBytes;

    public static ServerInternalsConfiguration defaultConfiguration() {
        return new ServerInternalsConfiguration(65536);
    }
}
