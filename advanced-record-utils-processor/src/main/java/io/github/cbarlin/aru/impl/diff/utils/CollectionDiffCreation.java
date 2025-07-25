package io.github.cbarlin.aru.impl.diff.utils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.diff.DifferVisitor;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHolder;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public final class CollectionDiffCreation extends DifferVisitor {

    public CollectionDiffCreation() {
        super(Claims.DIFFER_UTILS_COMPUTE_CHANGE);
    }

    private final Set<String> processedSpecs = HashSet.newHashSet(20);

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int innerSpecificity() {
        return 5;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        if (!(analysedComponent instanceof final AnalysedCollectionComponent acc)) {
            return false;
        }
        final Optional<CollectionHandler> handlerOptional = CollectionHandlerHolder.COLLECTION_HANDLERS
            .stream()
            .filter(c -> c.canHandle(analysedComponent))
            .findFirst();
        if (handlerOptional.isEmpty()) {
            return false;
        }
        final CollectionHandler handler = handlerOptional.get();
        final String methodName = hasChangedStaticMethodName(analysedComponent.typeName());
        if (!processedSpecs.add(methodName)) {
            return true;
        }
        final ToBeBuiltRecord innerRecord = collectionDiffRecord(acc);
        final TypeName innerType = acc.unNestedPrimaryTypeName();
        handler.addDiffRecordComponents(innerType, innerRecord);
        AnnotationSupplier.addGeneratedAnnotation(innerRecord, this);
        innerRecord.builder()
            .addOriginatingElement(acc.parentRecord().typeElement())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addJavadoc("A record containing the difference between two collections");
        final MethodSpec.Builder builder = differStaticClass.createMethod(methodName, claimableOperation)
            .addModifiers(Modifier.FINAL, Modifier.STATIC);
        AnnotationSupplier.addGeneratedAnnotation(builder, this);
        handler.writeDifferMethod(innerType, builder, innerRecord.className());
        return true;
    }
}
