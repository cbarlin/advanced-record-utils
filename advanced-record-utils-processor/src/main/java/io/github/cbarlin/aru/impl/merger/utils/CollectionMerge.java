package io.github.cbarlin.aru.impl.merger.utils;

import java.util.Set;

import javax.lang.model.element.Modifier;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.merger.MergerHolder;
import io.github.cbarlin.aru.impl.merger.MergerVisitor;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHelper;
import io.github.cbarlin.aru.impl.wiring.MergerPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import jakarta.inject.Singleton;

@Singleton
@MergerPerComponentScope
@RequiresBean({CollectionHandlerHelper.class})
public final class CollectionMerge extends MergerVisitor {

    private final Set<String> processedSpecs;
    private final CollectionHandlerHelper handler;

    public CollectionMerge(final MergerHolder mergerHolder, final CollectionHandlerHelper handler) {
        super(Claims.MERGER_ADD_FIELD_MERGER_METHOD, mergerHolder);
        this.processedSpecs = mergerHolder.processedMethods();
        this.handler = handler;
    }

    @Override
    protected int innerSpecificity() {
        return 3;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent acc) {
        final String methodName = mergeStaticMethodName(acc.typeName());
        if (processedSpecs.add(methodName)) {
            final MethodSpec.Builder method = mergerStaticClass.createMethod(methodName, claimableOperation);
            method.modifiers.clear();
            method.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
            handler.writeMergerMethod(method);
            AnnotationSupplier.addGeneratedAnnotation(method, this);
        }
        return true;
    }

}
