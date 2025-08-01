package io.github.cbarlin.aru.impl.builder.collection;

import java.util.Optional;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.CollectionRecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHolder;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public final class AddAddAllIterable extends CollectionRecordVisitor {

    public AddAddAllIterable() {
        super(Claims.BUILDER_ADD_ALL_ITERABLE);
    }

    @Override
    protected int collectionSpecificity() {
        return 1;
    }

    @Override
    public boolean isApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected boolean visitCollectionComponent(final AnalysedCollectionComponent acc) {
        if (acc.isIntendedConstructorParam()) {
            final Optional<CollectionHandler> handlerOptional = CollectionHandlerHolder.COLLECTION_HANDLERS
                .stream()
                .filter(c -> c.canHandle(acc))
                .findFirst();
            if (handlerOptional.isPresent()) {
                final CollectionHandler handler = handlerOptional.get();
                final var settings = acc.settings().prism().builderOptions();
                final boolean nonNull = !Boolean.FALSE.equals(settings.buildNullCollectionToEmpty());
                final boolean immutable = !"AUTO".equals(settings.builtCollectionType());
                final String addName = addNameMethodName(acc);
                final ToBeBuilt builderArtifact = acc.builderArtifact();
                final TypeName innerType = acc.unNestedPrimaryTypeName();

                if (nonNull && immutable) {
                    handler.writeNonNullImmutableAddManyToBuilder(acc, builderArtifact, innerType, addName, addName, this);
                } else if (nonNull) {
                    handler.writeNonNullAutoAddManyToBuilder(acc, builderArtifact, innerType, addName, addName, this);
                } else if (immutable) {
                    handler.writeNullableImmutableAddManyToBuilder(acc, builderArtifact, innerType, addName, addName, this);
                } else {
                    handler.writeNullableAutoAddManyToBuilder(acc, builderArtifact, innerType, addName, addName, this);
                }
                return true;
            }
        }
        return false;
    }
}
