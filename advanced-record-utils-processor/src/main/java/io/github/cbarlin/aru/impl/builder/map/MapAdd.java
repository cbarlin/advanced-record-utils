package io.github.cbarlin.aru.impl.builder.map;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants;
import io.github.cbarlin.aru.impl.types.AnalysedMapComponent;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.github.cbarlin.aru.prism.prison.BuilderOptionsPrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

import javax.lang.model.element.Modifier;
import java.util.HashMap;

@Component
@BuilderPerComponentScope
@RequiresProperty(value = "createAdderMethods", equalTo = "true")
@RequiresBean({ConstructorComponent.class, AnalysedMapComponent.class})
public final class MapAdd extends RecordVisitor {

    private final BuilderClass builderClass;
    private final AnalysedMapComponent analysedMapComponent;

    public MapAdd(
        final AnalysedRecord analysedRecord,
        final BuilderClass builderClass,
        final AnalysedMapComponent analysedMapComponent
    ) {
        super(CommonsConstants.Claims.CORE_BUILDER_SINGLE_ITEM_ADDER, analysedRecord);
        this.builderClass = builderClass;
        this.analysedMapComponent = analysedMapComponent;
    }

    @Override
    public int specificity() {
        return 3;
    }

    @Override
    protected boolean visitComponentImpl(
        final AnalysedComponent ignored
    ) {
        addTwoArgs();
        return true;
    }

    private void addTwoArgs() {
        final String methodName = addNameMethodName();
        final MethodSpec.Builder method = builderClass.createMethod(methodName, claimableOperation, analysedMapComponent.element());
        AnnotationSupplier.addGeneratedAnnotation(method, this);
        final BuilderOptionsPrism opts = analysedRecord.settings().prism().builderOptions();
        final String name = analysedMapComponent.name();
        final boolean immutable = !"AUTO".equals(opts.builtCollectionType());
        final ParameterSpec primaryParam = ParameterSpec.builder(
                        immutable ? analysedMapComponent.unNestedPrimaryTypeName().annotated(CommonsConstants.NON_NULL_ANNOTATION) : analysedMapComponent.unNestedPrimaryTypeName().withoutAnnotations(),
                "key",
                        Modifier.FINAL
                )
                .addJavadoc("The key of the instance to be added to the map")
                .build();
        final ParameterSpec secondaryParam = ParameterSpec.builder(
                        immutable ? analysedMapComponent.unNestedSecondaryTypeName().annotated(CommonsConstants.NON_NULL_ANNOTATION) : analysedMapComponent.unNestedSecondaryTypeName().withoutAnnotations(),
                        "value",
                        Modifier.FINAL
                )
                .addJavadoc("The value of the instance to be added to the map")
                .build();
        method.addJavadoc("Add a singular key/value pair to the map for the field {@code $L}", name)
                .returns(builderClass.className())
                .addParameter(primaryParam)
                .addParameter(secondaryParam)
                .addModifiers(Modifier.PUBLIC);
        method.beginControlFlow("if ($T.isNull(this.$L))", Constants.Names.OBJECTS, name)
                .addStatement("this.$L = new $T<>()", name, analysedMapComponent.mutableTypeName())
                .endControlFlow()
                .addStatement("this.$L.put(key, value)", name)
                .addStatement("return this");
    }

    private String addNameMethodName() {
        return analysedRecord.settings().prism().builderOptions().adderMethodPrefix() +
                capitalise(analysedMapComponent.name()) +
                analysedRecord.settings().prism().builderOptions().adderMethodSuffix();
    }
}
