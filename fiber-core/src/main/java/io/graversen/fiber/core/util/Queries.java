package io.graversen.fiber.core.util;

import lombok.experimental.UtilityClass;

import java.util.function.Predicate;

@UtilityClass
public class Queries {
    public static <T> Predicate<T> all() {
        return x -> true;
    }
}
