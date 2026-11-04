package com.example.DevTutor.util;

import java.util.Optional;

public final class Exceptions {

    private Exceptions() {}

    public static <T extends Throwable> Optional<T> findCause(Throwable ex, Class<T> type) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (type.isInstance(t)) {
                return Optional.of(type.cast(t));
            }
        }
        return Optional.empty();
    }
}
