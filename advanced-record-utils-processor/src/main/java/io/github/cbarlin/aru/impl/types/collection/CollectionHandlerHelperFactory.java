package io.github.cbarlin.aru.impl.types.collection;

import java.util.List;
import java.util.Optional;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.BasicAnalysedComponent;
import io.github.cbarlin.aru.impl.types.collection.wrapper.NonNullAutoCollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.wrapper.NonNullImmutableNullCollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.wrapper.NullableAutoCollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.wrapper.NullableImmutableCollectionHandler;
import io.github.cbarlin.aru.impl.wiring.BasePerComponentScope;
import io.github.cbarlin.aru.prism.prison.BuilderOptionsPrism;

@Factory
@BasePerComponentScope
public final class CollectionHandlerHelperFactory {

    @Bean
    @RequiresBean(AnalysedCollectionComponent.class)
    Optional<CollectionHandlerHelper> helper(
        final AnalysedCollectionComponent basicAnalysedComponent,
        final BuilderOptionsPrism builderOptionsPrism,
        final List<CollectionHandler> handlers
    ) {
        return handlers.stream()
                .filter(c -> c.canHandle(basicAnalysedComponent))
                .findFirst()
                .map(handler -> createHandler(basicAnalysedComponent, handler, builderOptionsPrism));
    }

    private CollectionHandlerHelper createHandler(final AnalysedCollectionComponent ac, final CollectionHandler handler, final BuilderOptionsPrism opts) {
        final boolean nonNull = !Boolean.FALSE.equals(opts.buildNullCollectionToEmpty());
        final boolean immutable = !"AUTO".equals(opts.builtCollectionType());
        final boolean nullReplacesNonNull = (!Boolean.FALSE.equals(opts.nullReplacesNotNull()));

        if (nonNull && immutable) {
            return new NonNullImmutableNullCollectionHandler(ac, handler, nullReplacesNonNull);
        } else if (nonNull) {
            return new NonNullAutoCollectionHandler(ac, handler, nullReplacesNonNull);
        } else if (immutable) {
            return new NullableImmutableCollectionHandler(ac, handler, nullReplacesNonNull);
        } else {
            return new NullableAutoCollectionHandler(ac, handler, nullReplacesNonNull);
        }
    }
}
