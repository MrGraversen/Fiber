package io.graversen.fiber.core.codec;

@FunctionalInterface
public interface IDecoder<T> {
    T decode(byte[] data);
}
