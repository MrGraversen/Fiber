package io.graversen.fiber.debug;

import io.graversen.fiber.core.codec.IEncoder;

public class ByteToByteEncoder implements IEncoder<byte[]> {
    @Override
    public byte[] encode(byte[] value) {
        return value;
    }
}
