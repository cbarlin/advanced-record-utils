package io.github.cbarlin.aru.impl.builder.collection;

import java.util.Optional;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.CollectionRecordVisitor;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHolder;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public final class AddGetter extends CollectionRecordVisitor {

    public AddGetter() {
        super(CommonsConstants.Claims.CORE_BUILDER_GETTER);
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
                final MethodSpec.Builder method = ac.builderArtifact()
                    .createMethod(ac.name(), claimableOperation, ac.element());

                if (nonNull && immutable) {
                    handler.writeNonNullImmutableGetter(ac, method, innerType);
                } else if (nonNull) {
                    handler.writeNonNullAutoGetter(ac, method, innerType);
                } else if (immutable) {
                    handler.writeNullableImmutableGetter(ac, method, innerType);
                } else {
                    handler.writeNullableAutoGetter(ac, method, innerType);
                }
                AnnotationSupplier.addGeneratedAnnotation(method, this);
                return true;
            }
        }
        return false;
    }
}
