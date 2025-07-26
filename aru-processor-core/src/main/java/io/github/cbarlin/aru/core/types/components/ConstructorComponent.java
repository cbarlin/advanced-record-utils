package io.github.cbarlin.aru.core.types.components;

import io.github.cbarlin.aru.core.types.AnalysedComponent;

public record ConstructorComponent(
    AnalysedComponent delegate
) implements DelegatingComponent {

}
