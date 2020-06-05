package io.graversen.fiber.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FiberEnvironment {
    public int availableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }
}
