package io.github.cbarlin.aru.annotations.aliases;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import io.github.cbarlin.aru.annotations.TypeAlias;

@NullMarked
public interface IntegerAlias extends TypeAlias<Integer>, Comparable<IntegerAlias> {

    @Override
    @NonNull Integer value();

    @Override
    default int compareTo(IntegerAlias o) {
        return value().compareTo(o.value());
    }
}
