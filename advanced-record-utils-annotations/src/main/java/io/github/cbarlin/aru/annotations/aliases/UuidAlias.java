package io.github.cbarlin.aru.annotations.aliases;

import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import io.github.cbarlin.aru.annotations.TypeAlias;

/**
 * An alias for {@link UUID} types, that also defers ordering to those UUIDs.
 */
@NullMarked
public interface UuidAlias extends TypeAlias<UUID>, Comparable<UuidAlias> {

    /**
     * @inheritdoc
     */
    @Override
    @NonNull UUID value();

    /**
     * Compares the UUIDs that are wrapped using their ordering.
     */
    @Override
    default int compareTo(UuidAlias o) {
        return value().compareTo(o.value());
    }
}
