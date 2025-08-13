package io.github.cbarlin.aru.impl.builder.collection;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.CollectionRecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.ComponentTargetingInterface;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHelper;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.github.cbarlin.aru.prism.prison.BuilderOptionsPrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
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
@RequiresBean({CollectionHandlerHelper.class, AnalysedCollectionComponent.class, ConstructorComponent.class, ComponentTargetingInterface.class})
public final class AddFluentAdderFromInterface extends CollectionRecordVisitor {

    private final AnalysedInterface target;
    private final BuilderClass builder;
    private final BuilderOptionsPrism myPrism;

    public AddFluentAdderFromInterface(
        final AnalysedCollectionComponent acc,
        final BuilderClass builderClass,
        final CollectionHandlerHelper minimalCollectionHandler,
        final ComponentTargetingInterface targetingInterface,
        final BuilderOptionsPrism myPrism
    ) {
        super(Claims.BUILDER_FLUENT_SETTER, acc.parentRecord(), acc);
        target = targetingInterface.target();
        this.builder = builderClass;
        this.myPrism = myPrism;
    }

    @Override
    public int collectionSpecificity() {
        return 3;
    }

    @Override
    protected boolean visitCollectionComponent() {
        
        final String addMethodName = addNameMethodName();
        analysedRecord.addCrossReference(target);
        for (final ProcessingTarget asTarget : target.implementingTypes()) {
            if (!concreteImplementingType(asTarget)) {
                continue;
            }
            analysedRecord.addCrossReference(asTarget);
            final String emptyMethodName = asTarget.prism().builderOptions().emptyCreationName();
            final String buildMethodName = asTarget.prism().builderOptions().buildMethodName();
            final ClassName otherBuilderClassName = asTarget.builderArtifact().className();
            final ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(CONSUMER, otherBuilderClassName);
            final ParameterSpec paramSpec = ParameterSpec.builder(paramTypeName, "subBuilder", Modifier.FINAL)
                    .addAnnotation(NON_NULL)
                    .addJavadoc("Builder used to invoke {@code $L}", addMethodName)
                    .build();

            final ClassName targetCN = ClassName.get(asTarget.typeElement());
            final String methodName = addCnToNameMethodName(targetCN);

            final MethodSpec.Builder methodBuilder = builder.createMethod(methodName, claimableOperation, asTarget.typeElement(), paramTypeName)
                .addAnnotation(NON_NULL)
                .returns(builder.className())
                .addParameter(paramSpec)
                .addJavadoc("Uses a supplied builder to build an instance of {@link $T} and add to the value of {@link $L}", targetCN, addMethodName)
                .addStatement("$T.requireNonNull(subBuilder, $S)", OBJECTS, "Cannot supply a null function argument")
                .addStatement("final $T builder = $T.$L()", otherBuilderClassName, otherBuilderClassName, emptyMethodName);

            logTrace(methodBuilder, "Passing over to provided consumer");
                
            methodBuilder.addStatement("subBuilder.accept(builder)")
                .addStatement("return this.$L(builder.$L())", addMethodName, buildMethodName);
            AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
            analysedCollectionComponent.addCrossReference(target);
        }
        return true;
    }

    private static boolean concreteImplementingType (final ProcessingTarget processingTarget) {
        return (processingTarget instanceof AnalysedRecord) || 
            (processingTarget instanceof LibraryLoadedTarget);
    }
}
