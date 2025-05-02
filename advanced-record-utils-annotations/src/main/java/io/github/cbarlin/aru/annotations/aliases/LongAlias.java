package io.github.cbarlin.aru.annotations.aliases;

import org.jspecify.annotations.NonNull;

import io.github.cbarlin.aru.annotations.TypeAlias;

public interface LongAlias extends TypeAlias<Long>, Comparable<LongAlias> {

    @Override
    @NonNull Long value();

    @Override
    default int compareTo(LongAlias o) {
        return value().compareTo(o.value());
    }
}
