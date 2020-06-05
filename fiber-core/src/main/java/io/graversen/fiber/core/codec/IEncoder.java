package io.graversen.fiber.core.codec;

@FunctionalInterface
public interface IEncoder<T> {
    byte[] encode(T value);
}
