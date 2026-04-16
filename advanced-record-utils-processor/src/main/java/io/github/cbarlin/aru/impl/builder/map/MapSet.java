package io.github.cbarlin.aru.impl.builder.map;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.types.maps.MapHandlerHelper;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

@Component
@BuilderPerComponentScope
@RequiresBean({ConstructorComponent.class, MapHandlerHelper.class})
public final class MapSet extends RecordVisitor {

    private final BuilderClass builderClass;
    private final MapHandlerHelper mapHandlerHelper;

    public MapSet(
            final AnalysedRecord analysedRecord,
            final BuilderClass builderClass,
            final MapHandlerHelper mapHandlerHelper
    ) {
        super(CommonsConstants.Claims.CORE_BUILDER_SETTER, analysedRecord);
        this.builderClass = builderClass;
        this.mapHandlerHelper = mapHandlerHelper;
    }

    @Override
    public int specificity() {
        return 6;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final MethodSpec.Builder method = builderClass
                .createMethod(analysedComponent.name(), claimableOperation, analysedComponent.element());
        final String name = analysedComponent.name();
        method.addJavadoc("Updates the value of {@code $L}", name)
                .returns(builderClass.className())
                .addAnnotation(CommonsConstants.Names.NON_NULL)
                .addModifiers(Modifier.PUBLIC);
        if (mapHandlerHelper.nullReplacesNotNull()) {
            method.addJavadoc("\n<p>\n")
                            .addJavadoc("Supplying a null value will clear the current map");
        } else {
            method.addJavadoc("\n<p>\n")
                            .addJavadoc("Supplying a null value won't replace a set value");
        }
        mapHandlerHelper.writeSetter(method);
        AnnotationSupplier.addGeneratedAnnotation(method, this);
        method.addStatement("return this");
        return true;
    }
}
