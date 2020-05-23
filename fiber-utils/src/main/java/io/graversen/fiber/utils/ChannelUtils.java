package io.graversen.fiber.utils;

import lombok.experimental.UtilityClass;

import java.nio.channels.AsynchronousSocketChannel;

@UtilityClass
public class ChannelUtils {
    public static void close(AsynchronousSocketChannel channel) {
        try {
            channel.close();
            channel.shutdownInput();
            channel.shutdownOutput();
        } catch (Exception e) {
            // Nothing
        }
    }
}
