package io.github.cbarlin.aru.impl.merger.utils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.merger.MergerVisitor;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHolder;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public final class CollectionMerge extends MergerVisitor {

    private final Set<String> processedSpecs = HashSet.newHashSet(5);

    public CollectionMerge() {
        super(Claims.MERGER_ADD_FIELD_MERGER_METHOD);
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int innerSpecificity() {
        return 3;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        if (analysedComponent instanceof final AnalysedCollectionComponent acc) {
            final Optional<CollectionHandler> handlerOptional = CollectionHandlerHolder.COLLECTION_HANDLERS
                .stream()
                .filter(c -> c.canHandle(analysedComponent))
                .findFirst();
            if (handlerOptional.isPresent()) {
                final CollectionHandler handler = handlerOptional.get();
                final TypeName innerTypeName = acc.unNestedPrimaryTypeName();
                final var targetTn = analysedComponent.typeName();
                final String methodName = mergeStaticMethodName(targetTn);
                if (processedSpecs.add(methodName)) {
                    final MethodSpec.Builder method = mergerStaticClass.createMethod(methodName, claimableOperation);
                    method.modifiers.clear();
                    method.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
                    handler.writeMergerMethod(innerTypeName, method);
                    AnnotationSupplier.addGeneratedAnnotation(method, this);
                }
                return true;
            }
        }
        return false;
    }

}
