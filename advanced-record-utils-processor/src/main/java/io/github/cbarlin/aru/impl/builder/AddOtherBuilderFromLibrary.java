package io.github.cbarlin.aru.impl.builder;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.ComponentTargetingLibraryLoaded;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.impl.Constants.Names.CONSUMER;
import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;

@Component
@BuilderPerComponentScope
@RequiresProperty(value = "fluent", equalTo = "true")
@RequiresBean({ConstructorComponent.class, ComponentTargetingLibraryLoaded.class})
public final class AddOtherBuilderFromLibrary extends RecordVisitor {

    private final ComponentTargetingLibraryLoaded cti;
    private final BuilderClass builder;

    public AddOtherBuilderFromLibrary(final ComponentTargetingLibraryLoaded cti, final BuilderClass builderClass) {
        super(Claims.BUILDER_OTHER_BUILDER, cti.parentRecord());
        this.cti = cti;
        this.builder = builderClass;
    }

    @Override
    public int specificity() {
        return 3;
    }
    
    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final LibraryLoadedTarget other = cti.target();
        final String name = analysedComponent.name();
        analysedRecord.addCrossReference(other);

        final String buildMethodName = other.prism().builderOptions().buildMethodName();

        final ClassName otherBuilderClassName = other.builderArtifact().className();
        // Then the consumer version
        final ParameterSpec paramSpec = ParameterSpec.builder(otherBuilderClassName, "otherBuilder", Modifier.FINAL)
                .addJavadoc("Builder that can be used to replace {@code $L}", name)
                .build();
        final var methodBuilder = builder.createMethod(analysedComponent.name(), claimableOperation, analysedComponent, CONSUMER)
            .returns(builder.className())
            .addParameter(paramSpec)
            .addJavadoc("Uses a supplied builder to replace the value at {@code $L}", name)
            .addStatement("$T.requireNonNull(otherBuilder, $S)", OBJECTS, "Cannot supply a null function argument")
            .addStatement("return this.$L(otherBuilder.$L())", name, buildMethodName);
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        analysedComponent.addCrossReference(other);
        return true;
    }

}
