package io.github.cbarlin.aru.core.types.components;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedTypeConverter;

import java.util.Set;

public record TypeConverterComponent(
        AnalysedComponent delegate,
        Set<AnalysedTypeConverter> analysedTypeConverters
) implements DelegatingComponent {
}
