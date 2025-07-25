package io.github.cbarlin.aru.impl.builder.collection;

import java.util.Optional;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.CollectionRecordVisitor;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHolder;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public final class AddField extends CollectionRecordVisitor {

    public AddField() {
        super(CommonsConstants.Claims.CORE_BUILDER_FIELD);
    }

    @Override
    public int collectionSpecificity() {
        return 5;
    }

    @Override
    public boolean isApplicable(final AnalysedRecord target) {
        return true;
    }

    @Override
    protected boolean visitCollectionComponent(final AnalysedCollectionComponent ac) {
        if (ac.isIntendedConstructorParam()) {
            final Optional<CollectionHandler> handlerOptional = CollectionHandlerHolder.COLLECTION_HANDLERS
                .stream()
                .filter(c -> c.canHandle(ac))
                .findFirst();
            if (handlerOptional.isPresent()) {
                final CollectionHandler handler = handlerOptional.get();
                final var settings = ac.settings().prism().builderOptions();
                final boolean nonNull = !Boolean.FALSE.equals(settings.buildNullCollectionToEmpty());
                final boolean immutable = !"AUTO".equals(settings.builtCollectionType());
                final TypeName innerType = ac.unNestedPrimaryTypeName();
                final ToBeBuilt builder = ac.builderArtifact();

                if (nonNull && immutable) {
                    handler.addNonNullImmutableField(ac, builder, innerType);
                } else if (nonNull) {
                    handler.addNonNullAutoField(ac, builder, innerType);
                } else if (immutable) {
                    handler.addNullableImmutableField(ac, builder, innerType);
                } else {
                    handler.addNullableAutoField(ac, builder, innerType);
                }

                return true;
            }
        }
        return false;
    }
}
