package io.graversen.fiber.event;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class EventSignature {
    private final Class<?> eventClass;
}
