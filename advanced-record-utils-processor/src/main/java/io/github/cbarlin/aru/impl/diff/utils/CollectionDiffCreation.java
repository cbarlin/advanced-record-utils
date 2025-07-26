package io.github.cbarlin.aru.impl.diff.utils;

import java.util.Set;

import javax.lang.model.element.Modifier;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.diff.DifferVisitor;
import io.github.cbarlin.aru.impl.diff.holders.DiffHolder;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHelper;
import io.github.cbarlin.aru.impl.wiring.DiffPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import jakarta.inject.Singleton;

@Singleton
@DiffPerComponentScope
@RequiresBean({CollectionHandlerHelper.class})
public final class CollectionDiffCreation extends DifferVisitor {

    private final CollectionHandlerHelper handler;
    private final Set<String> processedSpecs;

    public CollectionDiffCreation(final DiffHolder diffHolder, final CollectionHandlerHelper helper) {
        super(Claims.DIFFER_UTILS_COMPUTE_CHANGE, diffHolder);
        this.processedSpecs = diffHolder.staticClass().createdMethods();
        this.handler = helper;
    }

    @Override
    protected int innerSpecificity() {
        return 5;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent acc) {
        final String methodName = hasChangedStaticMethodName(acc.typeName());
        if (!processedSpecs.add(methodName)) {
            return true;
        }
        final ToBeBuiltRecord innerRecord = collectionDiffRecord(acc);
        handler.addDiffRecordComponents(innerRecord);
        AnnotationSupplier.addGeneratedAnnotation(innerRecord, this);
        innerRecord.builder()
            .addOriginatingElement(acc.parentRecord().typeElement())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addJavadoc("A record containing the difference between two collections");
        final MethodSpec.Builder builder = differStaticClass.createMethod(methodName, claimableOperation)
            .addModifiers(Modifier.FINAL, Modifier.STATIC);
        AnnotationSupplier.addGeneratedAnnotation(builder, this);
        handler.writeDifferMethod(builder, innerRecord.className());
        return true;
    }
}
