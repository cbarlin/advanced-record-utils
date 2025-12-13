package io.github.cbarlin.aru.impl.builder;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.AnalysedOptionalPrimitiveComponent;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

import javax.lang.model.element.Modifier;

@Component
@BuilderPerComponentScope
@RequiresBean({ConstructorComponent.class, AnalysedOptionalPrimitiveComponent.class})
public final class AddOptionalPrimitive extends RecordVisitor {

    private final AnalysedOptionalPrimitiveComponent component;
    private final BuilderClass builder;

    public AddOptionalPrimitive(final AnalysedOptionalPrimitiveComponent component, final BuilderClass builderClass) {
        super(Claims.BUILDER_CONCRETE_OPTIONAL, component.parentRecord());
        this.builder = builderClass;
        this.component = component;
    }

    @Override
    public int specificity() {
        return 1;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final ClassName builderClassName = builder.className();
        final String name = component.name();
        final ParameterSpec param = ParameterSpec.builder(component.unNestedPrimaryTypeName(), name, Modifier.FINAL)
            .addJavadoc("The replacement value")
            .build();
        final MethodSpec.Builder method = builder.createMethod(name, claimableOperation, analysedComponent)
            .addJavadoc("Updates the value of {@code $L}", name)
            .returns(builderClassName)
            .addParameter(param)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return this.$L($T.of($L))", name, component.typeName(), name);
        AnnotationSupplier.addGeneratedAnnotation(method, this);
        return true;
    }    
}
