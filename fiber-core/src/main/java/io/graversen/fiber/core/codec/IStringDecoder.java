package io.graversen.fiber.core.codec;

import java.nio.charset.Charset;

@FunctionalInterface
public interface IStringDecoder<T> extends IDecoder<T> {
    @Override
    default T decode(byte[] data) {
        return decode(new String(data, Charset.defaultCharset()));
    }

    T decode(String data);
}
