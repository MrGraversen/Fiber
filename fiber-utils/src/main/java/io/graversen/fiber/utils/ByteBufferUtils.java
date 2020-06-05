package io.graversen.fiber.utils;

import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@UtilityClass
public class ByteBufferUtils {
    public static ByteBuffer from(String string) {
        return ByteBuffer.wrap(string.getBytes(Charset.defaultCharset()));
    }
}
