package io.github.cbarlin.aru.annotations.aliases;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import io.github.cbarlin.aru.annotations.TypeAlias;

/**
 * An alias for {@link Long} types, that also defers ordering to those longs.
 */
@NullMarked
public interface LongAlias extends TypeAlias<Long>, Comparable<LongAlias> {

    /**
     * @inheritdoc
     */
    @Override
    @NonNull Long value();

    /**
     * Compares the longs that are wrapped using their ordering.
     */
    @Override
    default int compareTo(LongAlias o) {
        return value().compareTo(o.value());
    }
}
