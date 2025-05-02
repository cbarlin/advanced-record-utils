package io.github.cbarlin.aru.annotations.aliases;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import io.github.cbarlin.aru.annotations.TypeAlias;

@NullMarked
public interface LongAlias extends TypeAlias<Long>, Comparable<LongAlias> {

    @Override
    @NonNull Long value();

    @Override
    default int compareTo(LongAlias o) {
        return value().compareTo(o.value());
    }
}
