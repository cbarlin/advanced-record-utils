package io.github.cbarlin.aru.annotations.aliases;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import io.github.cbarlin.aru.annotations.TypeAlias;

/**
 * An alias for {@link Integer} types, that also defers ordering to those integers.
 */
@NullMarked
public interface IntegerAlias extends TypeAlias<Integer>, Comparable<IntegerAlias> {

    @Override
    @NonNull Integer value();

    /**
     * Compares the integers that are wrapped using their ordering.
     */
    @Override
    default int compareTo(IntegerAlias o) {
        return value().compareTo(o.value());
    }
}
