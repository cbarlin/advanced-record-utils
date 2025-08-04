package io.github.cbarlin.aru.impl.types;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.components.DelegatingComponent;

public record ComponentTargetingInterface(
    AnalysedComponent delegate,
    AnalysedInterface target
) implements DelegatingComponent {

}
