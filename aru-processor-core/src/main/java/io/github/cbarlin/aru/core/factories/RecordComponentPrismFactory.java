package io.github.cbarlin.aru.core.factories;

import io.avaje.inject.BeanTypes;
import org.jspecify.annotations.NullUnmarked;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.types.components.BasicAnalysedComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.wiring.CorePerComponentScope;

import java.util.Optional;

@Factory
@NullUnmarked
@CorePerComponentScope
public final class RecordComponentPrismFactory {

    @Bean
    @BeanTypes(ConstructorComponent.class)
    Optional<ConstructorComponent> constructorComponent(final BasicAnalysedComponent basicAnalysedComponent) {
        if (basicAnalysedComponent.isIntendedConstructorParam()) {
            return Optional.of(new ConstructorComponent(basicAnalysedComponent));
        }
        return Optional.empty();
    }

}
