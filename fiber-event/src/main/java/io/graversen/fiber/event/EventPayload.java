package io.graversen.fiber.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
class EventPayload {
    private final @NonNull IEvent event;
    private final Duration expiration;

    public LocalDateTime expiresAt() {
        return expiration != null ? event.eventEmittedAt().plus(expiration) : LocalDateTime.MAX;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt());
    }
}
