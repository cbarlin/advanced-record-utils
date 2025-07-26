package io.github.cbarlin.aru.impl.types;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.DelegatingComponent;

public record ComponentTargetingRecord(
    AnalysedComponent delegate,
    AnalysedRecord target
) implements DelegatingComponent {

}
