package io.graversen.fiber.util;

public final class Environment
{
    private Environment()
    {

    }

    public static int availableProcessors()
    {
        return Runtime.getRuntime().availableProcessors();
    }
}
