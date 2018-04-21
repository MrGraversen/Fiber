package io.graversen.fiber.server.management;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Stream;

public final class NetworkMessage
{
    private final byte[] messageData;

    public NetworkMessage(byte[] messageData)
    {
        this.messageData = messageData;
    }

    public byte[] getMessageData()
    {
        return messageData;
    }

    public Stream<Byte> getMessageDataStream()
    {
        final Byte[] byteArray = new Byte[messageData.length];
        Arrays.setAll(byteArray, n -> messageData[n]);

        return Stream.of(byteArray);
    }

    public ByteBuffer getMessageDataBuffer()
    {
        return ByteBuffer.wrap(messageData);
    }
}
