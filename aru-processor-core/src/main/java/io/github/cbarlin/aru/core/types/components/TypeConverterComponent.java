package io.github.cbarlin.aru.core.types.components;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedTypeConverter;

import java.util.List;

public record TypeConverterComponent(
        AnalysedComponent delegate,
        List<AnalysedTypeConverter> analysedTypeConverters
) implements DelegatingComponent {
}
