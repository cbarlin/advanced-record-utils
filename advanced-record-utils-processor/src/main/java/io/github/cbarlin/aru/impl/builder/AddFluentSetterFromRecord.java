package io.github.cbarlin.aru.impl.builder;

import static io.github.cbarlin.aru.impl.Constants.Names.CONSUMER;
import static io.github.cbarlin.aru.impl.Constants.Names.NON_NULL;
import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;

import javax.lang.model.element.Modifier;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.ComponentTargetingRecord;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;

@Component
@BuilderPerComponentScope
@RequiresProperty(value = "fluent", equalTo = "true")
@RequiresBean({ConstructorComponent.class, ComponentTargetingRecord.class})
public final class AddFluentSetterFromRecord extends RecordVisitor {

    private final ComponentTargetingRecord cti;
    private final BuilderClass builder;

    public AddFluentSetterFromRecord(final ComponentTargetingRecord cti, final BuilderClass builderClass) {
        super(Claims.BUILDER_FLUENT_SETTER, cti.parentRecord());
        this.cti = cti;
        this.builder = builderClass;
    }

    @Override
    public int specificity() {
        return 3;
    }
    
    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {

        final AnalysedRecord other = cti.target();
        final String name = analysedComponent.name();

        final String emptyMethodName = other.prism().builderOptions().emptyCreationName();
        final String copyMethodName = other.prism().builderOptions().copyCreationName();
        final String buildMethodName = other.prism().builderOptions().buildMethodName();

        final ClassName otherBuilderClassName = other.builderArtifact().className();
        // Then the consumer version
        final ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(CONSUMER, otherBuilderClassName);
        final ParameterSpec paramSpec = ParameterSpec.builder(paramTypeName, "subBuilder", Modifier.FINAL)
                .addAnnotation(NON_NULL)
                .addJavadoc("Builder that can be used to replace {@code $L}", name)
                .build();
        final var methodBuilder = builder.createMethod(analysedComponent.name(), claimableOperation, analysedComponent, CONSUMER)
            .addAnnotation(NON_NULL)
            .returns(builder.className())
            .addParameter(paramSpec)
            .addJavadoc("Uses a supplied builder to replace the value at {@code $L}", name)
            .addStatement("$T.requireNonNull(subBuilder, $S)", OBJECTS, "Cannot supply a null function argument")
            .addStatement("final $T builder = ($T.isNull(this.$L())) ? $T.$L() : $T.$L(this.$L())", otherBuilderClassName, OBJECTS, name, otherBuilderClassName, emptyMethodName, otherBuilderClassName, copyMethodName, name);
        
        logTrace(methodBuilder, "Passing over to provided consumer");
        
        methodBuilder.addStatement("subBuilder.accept(builder)")
            .addStatement("return this.$L(builder.$L())", name, buildMethodName);
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        analysedComponent.addCrossReference(other);
        return true;
    }

}
