package io.github.cbarlin.aru.impl.merger.utils;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.merger.MergerHolder;
import io.github.cbarlin.aru.impl.merger.MergerVisitor;
import io.github.cbarlin.aru.impl.types.maps.MapHandlerHelper;
import io.github.cbarlin.aru.impl.wiring.MergerPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import jakarta.inject.Singleton;

import javax.lang.model.element.Modifier;
import java.util.Set;

@Singleton
@MergerPerComponentScope
@RequiresBean({MapHandlerHelper.class})
public final class MapMerge extends MergerVisitor {

    private final Set<String> processedSpecs;
    private final MapHandlerHelper handler;

    public MapMerge(final MergerHolder mergerHolder, final MapHandlerHelper handler) {
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
