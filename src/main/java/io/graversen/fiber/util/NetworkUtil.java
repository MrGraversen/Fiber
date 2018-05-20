package io.graversen.fiber.util;

public class NetworkUtil
{
    private NetworkUtil()
    {

    }

    public static String getConnectionTuple(String ip, int port)
    {
        return String.format("%s:%d", ip, port);
    }
}
