package io.graversen.fiber.utils;

import lombok.experimental.UtilityClass;

import java.util.Random;
import java.util.UUID;

@UtilityClass
public class IdUtils {
    private final Random RANDOM = new Random();

    public String fastEventId() {
        return new UUID(RANDOM.nextLong(), RANDOM.nextLong()).toString();
    }

    public String fastClientId() {
        return new UUID(RANDOM.nextLong(), RANDOM.nextLong()).toString();
    }
}
