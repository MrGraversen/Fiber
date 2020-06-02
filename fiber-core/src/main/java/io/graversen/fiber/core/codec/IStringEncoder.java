package io.graversen.fiber.core.codec;

import java.nio.charset.Charset;

@FunctionalInterface
public interface IStringEncoder<T> extends IEncoder<T> {
    @Override
    default byte[] encode(T value) {
        return encodeString(value).getBytes(Charset.defaultCharset());
    }

    String encodeString(T value);
}
