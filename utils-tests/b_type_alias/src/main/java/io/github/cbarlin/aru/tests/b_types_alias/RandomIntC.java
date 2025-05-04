package io.github.cbarlin.aru.tests.b_types_alias;

import org.jspecify.annotations.NonNull;

import io.github.cbarlin.aru.annotations.aliases.IntegerAlias;

public class RandomIntC implements IntegerAlias {

    private final int wrapped;

    public RandomIntC(int v) {
        this.wrapped = v;
    }

    @Override
    @NonNull
    public Integer value() {
        return Integer.valueOf(wrapped);
    }
}
