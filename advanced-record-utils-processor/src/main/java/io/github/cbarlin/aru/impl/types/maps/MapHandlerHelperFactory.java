package io.github.cbarlin.aru.impl.types.maps;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.BasicAnalysedComponent;
import io.github.cbarlin.aru.impl.types.maps.wrapper.NonNullAutoMapHandler;
import io.github.cbarlin.aru.impl.types.maps.wrapper.NonNullImmutableMapHandler;
import io.github.cbarlin.aru.impl.types.maps.wrapper.NullableAutoMapHandler;
import io.github.cbarlin.aru.impl.types.maps.wrapper.NullableImmutableMapHandler;
import io.github.cbarlin.aru.impl.wiring.BasePerComponentScope;
import io.github.cbarlin.aru.prism.prison.BuilderOptionsPrism;

import java.util.List;
import java.util.Optional;

@Factory
@BasePerComponentScope
public final class MapHandlerHelperFactory {

    @Bean
    Optional<MapHandlerHelper> mapHandlerHelper (
        final BasicAnalysedComponent basicAnalysedComponent,
        final BuilderOptionsPrism builderOptionsPrism,
        final List<MapHandler> handlers
    ) {
        if (
            !basicAnalysedComponent.typeName().toString().contains("Map")
        ) {
            return Optional.empty();
        }
        return handlers.stream()
                .filter(c -> c.canHandle(basicAnalysedComponent))
                .findFirst()
                .map(handler -> createHelper(basicAnalysedComponent, handler, builderOptionsPrism));
    }

    private static MapHandlerHelper createHelper(final AnalysedComponent component, final MapHandler handler, final BuilderOptionsPrism opts) {
        final boolean nonNull = !Boolean.FALSE.equals(opts.buildNullCollectionToEmpty());
        final boolean immutable = !"AUTO".equals(opts.builtCollectionType());
        final boolean nullReplacesNotNull = (!Boolean.FALSE.equals(opts.nullReplacesNotNull()));

        if (nonNull && immutable) {
            return new NonNullImmutableMapHandler(component, handler, nullReplacesNotNull);
        } else if (nonNull) {
            return new NonNullAutoMapHandler(component, handler, nullReplacesNotNull);
        } else if (immutable) {
            return new NullableImmutableMapHandler(component, handler, nullReplacesNotNull);
        } else {
            return new NullableAutoMapHandler(component, handler, nullReplacesNotNull);
        }
    }
}
