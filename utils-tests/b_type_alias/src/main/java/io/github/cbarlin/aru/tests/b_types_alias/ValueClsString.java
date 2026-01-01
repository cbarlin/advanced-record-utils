package io.github.cbarlin.aru.tests.b_types_alias;

import com.tguzik.traits.HasStringValue;

public record ValueClsString(String get) implements HasStringValue {
}
