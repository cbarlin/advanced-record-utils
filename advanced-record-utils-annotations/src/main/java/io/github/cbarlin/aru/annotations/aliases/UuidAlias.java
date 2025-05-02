package io.github.cbarlin.aru.annotations.aliases;

import java.util.UUID;

import org.jspecify.annotations.NonNull;

import io.github.cbarlin.aru.annotations.TypeAlias;

public interface UuidAlias extends TypeAlias<UUID>, Comparable<UuidAlias> {

    @Override
    @NonNull UUID value();

    @Override
    default int compareTo(UuidAlias o) {
        return value().compareTo(o.value());
    }
}
