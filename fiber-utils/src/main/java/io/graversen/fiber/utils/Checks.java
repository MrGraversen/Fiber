package io.graversen.fiber.utils;

import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public class Checks {
    public static <T> T nonNull(T value, String parameterName) {
        return Objects.requireNonNull(value, String.format("Parameter '%s' must not be null", parameterName));
    }
}
