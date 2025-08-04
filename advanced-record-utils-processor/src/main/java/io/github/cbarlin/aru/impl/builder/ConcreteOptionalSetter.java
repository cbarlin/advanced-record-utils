package io.github.cbarlin.aru.impl.builder;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NOT_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;

import javax.lang.model.element.Modifier;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedOptionalComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@Component
@BuilderPerComponentScope
@RequiresProperty(value = "concreteSettersForOptional", equalTo = "true")
@RequiresBean({ConstructorComponent.class, AnalysedOptionalComponent.class})
public final class ConcreteOptionalSetter extends RecordVisitor {
    private final BuilderClass builderClass;
    private final AnalysedOptionalComponent aoc;

    public ConcreteOptionalSetter(final AnalysedOptionalComponent aoc, final BuilderClass builderClass) {
        super(Claims.BUILDER_CONCRETE_OPTIONAL, aoc.parentRecord());
        this.builderClass = builderClass;
        this.aoc = aoc;
    }

    @Override
    public int specificity() {
        return 1;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        final ClassName builderClassName = builderClass.className();
        final String name = analysedComponent.name();
        final ParameterSpec param = ParameterSpec.builder(aoc.unNestedPrimaryTypeName(), name, Modifier.FINAL)
            .addJavadoc("The replacement value")
            .addAnnotation(NULLABLE)
            .build();
        
        final MethodSpec.Builder method = builderClass.createMethod(name, claimableOperation, analysedComponent)
            .addJavadoc("Updates the value of {@code $L}", name)
            .returns(builderClassName)
            .addParameter(param)
            .addAnnotation(NOT_NULL)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return this.$L($T.ofNullable($L))", name, OPTIONAL, name);
        AnnotationSupplier.addGeneratedAnnotation(method, this);
        return true;
    }
}
