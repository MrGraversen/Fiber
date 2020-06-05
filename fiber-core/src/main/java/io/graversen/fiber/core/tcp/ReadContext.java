package io.graversen.fiber.core.tcp;

import lombok.Value;

import java.nio.ByteBuffer;

@Value
public class ReadContext {
    private final ITcpNetworkClient networkClient;
    private final ByteBuffer readBuffer;
}
