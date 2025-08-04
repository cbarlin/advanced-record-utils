package io.github.cbarlin.aru.impl.builder.collection;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.CollectionRecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.ComponentTargetingLibraryLoaded;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHelper;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.impl.Constants.Names.CONSUMER;
import static io.github.cbarlin.aru.impl.Constants.Names.NON_NULL;
import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;

@Component
@BuilderPerComponentScope
@RequiresProperty(value = "fluent", equalTo = "true")
@RequiresProperty(value = "createAdderMethods", equalTo = "true")
@RequiresBean({CollectionHandlerHelper.class, AnalysedCollectionComponent.class, ConstructorComponent.class, ComponentTargetingLibraryLoaded.class})
public final class AddFluentAdderFromLibrary extends CollectionRecordVisitor {

    private final LibraryLoadedTarget target;
    private final BuilderClass builder;

    public AddFluentAdderFromLibrary(
        final AnalysedCollectionComponent acc,
        final BuilderClass builderClass,
        final CollectionHandlerHelper minimalCollectionHandler,
        final ComponentTargetingLibraryLoaded targetingInterface
    ) {
        super(Claims.BUILDER_FLUENT_SETTER, acc.parentRecord(), acc);
        target = targetingInterface.target();
        this.builder = builderClass;
    }

    @Override
    public int collectionSpecificity() {
        return 3;
    }

    @Override
    protected boolean visitCollectionComponent() {
        final String name = analysedCollectionComponent.name();
        analysedRecord.addCrossReference(target);

        final String emptyMethodName = target.prism().builderOptions().emptyCreationName();
        final String buildMethodName = target.prism().builderOptions().buildMethodName();

        final ClassName otherBuilderClassName = target.builderArtifact().className();
        final ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(CONSUMER, otherBuilderClassName);
        final ParameterSpec paramSpec = ParameterSpec.builder(paramTypeName, "subBuilder", Modifier.FINAL)
                .addAnnotation(NON_NULL)
                .addJavadoc("Builder that can be used to replace {@code $L}", name)
                .build();
        final var methodBuilder = builder.createMethod(addNameMethodName(), claimableOperation, analysedCollectionComponent, CONSUMER)
            .addAnnotation(NON_NULL)
            .returns(builder.className())
            .addParameter(paramSpec)
            .addJavadoc("Uses a supplied builder to replace the value at {@code $L}", name)
            .addStatement("$T.requireNonNull(subBuilder, $S)", OBJECTS, "Cannot supply a null function argument")
            .addStatement("final $T builder = $T.$L()", otherBuilderClassName, otherBuilderClassName, emptyMethodName);
        
        logTrace(methodBuilder, "Passing over to provided consumer");
        
        methodBuilder.addStatement("subBuilder.accept(builder)")
            .addStatement("return this.$L(builder.$L())", addNameMethodName(), buildMethodName);
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        analysedCollectionComponent.addCrossReference(target);
        return true;
    }
}
