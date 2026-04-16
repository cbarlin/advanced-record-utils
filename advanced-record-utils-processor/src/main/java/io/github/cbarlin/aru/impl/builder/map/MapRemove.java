package io.github.cbarlin.aru.impl.builder.map;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants;
import io.github.cbarlin.aru.impl.types.maps.MapHandlerHelper;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

@Component
@BuilderPerComponentScope
@RequiresProperty(value = "createAdderMethods", equalTo = "true")
@RequiresBean({ConstructorComponent.class, MapHandlerHelper.class})
public final class MapRemove extends RecordVisitor {

    private final BuilderClass builderClass;
    private final MapHandlerHelper mapHandlerHelper;

    public MapRemove(
        final AnalysedRecord analysedRecord,
        final BuilderClass builderClass,
        final MapHandlerHelper mapHandlerHelper
    ) {
        super(Constants.Claims.BUILDER_REMOVE_SINGLE, analysedRecord);
        this.builderClass = builderClass;
        this.mapHandlerHelper = mapHandlerHelper;
    }

    @Override
    public int specificity() {
        return 6;
    }

    @Override
    protected boolean visitComponentImpl(
        final AnalysedComponent ignored
    ) {
        final String methodName = removeMethodName();
        final MethodSpec.Builder method = builderClass.createMethod(methodName, claimableOperation, mapHandlerHelper.component().element());
        AnnotationSupplier.addGeneratedAnnotation(method, this);
        mapHandlerHelper.writeRemoveSingle(method);
        method.addJavadoc("Remove a singular key from the map of {@code $L}", mapHandlerHelper.component().name())
            .returns(builderClass.className())
            .addModifiers(Modifier.PUBLIC);
        method.addStatement("return this");
        return true;
    }

    private String removeMethodName() {
        return analysedRecord.settings().prism().builderOptions().removeMethodPrefix() +
                capitalise(mapHandlerHelper.component().name()) +
                analysedRecord.settings().prism().builderOptions().removeMethodSuffix();
    }
}
