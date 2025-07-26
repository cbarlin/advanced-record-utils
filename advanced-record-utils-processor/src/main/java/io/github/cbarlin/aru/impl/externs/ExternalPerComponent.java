package io.github.cbarlin.aru.impl.externs;

import io.avaje.inject.Component;
import io.avaje.inject.External;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedOptionalComponent;
import io.github.cbarlin.aru.core.types.components.BasicAnalysedComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.types.components.TypeConverterComponent;
import io.github.cbarlin.aru.impl.wiring.BasePerComponentScope;

import javax.lang.model.element.RecordComponentElement;
import java.util.Optional;

@Component
@BasePerComponentScope
public record ExternalPerComponent(
        @External BasicAnalysedComponent basicAnalysedComponent,
        @External AnalysedComponent analysedComponent,
        @External Optional<AnalysedCollectionComponent> acc,
        @External Optional<AnalysedOptionalComponent> aoc,
        @External Optional<ConstructorComponent> cc,
        @External RecordComponentElement rce,
        @External Optional<TypeConverterComponent> tcc
) {

}
