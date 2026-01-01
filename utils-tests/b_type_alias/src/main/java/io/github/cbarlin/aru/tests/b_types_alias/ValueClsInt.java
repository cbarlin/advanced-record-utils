package io.github.cbarlin.aru.tests.b_types_alias;

import com.tguzik.traits.HasValue;

public record ValueClsInt(Integer get) implements HasValue<Integer> {
}
