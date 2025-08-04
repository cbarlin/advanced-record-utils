package io.github.cbarlin.aru.impl.types;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.types.components.DelegatingComponent;

public record ComponentTargetingLibraryLoaded(
    AnalysedComponent delegate,
    LibraryLoadedTarget target
) implements DelegatingComponent {

}
