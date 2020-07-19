package io.graversen.fiber.debug;

import io.graversen.fiber.core.codec.IDecoder;

public class ByteToByteDecoder implements IDecoder<byte[]> {
    @Override
    public byte[] decode(byte[] data) {
        return data;
    }
}
