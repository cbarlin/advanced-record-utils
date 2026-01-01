package io.github.cbarlin.aru.tests.b_types_alias;

import com.tguzik.traits.HasValue;

public record ValueClsLong(long value) implements HasValue<Long> {
    @Override
    public Long get() {
        return value;
    }
}
