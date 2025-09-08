package io.github.cbarlin.aru.impl.builder.collection;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.CollectionRecordVisitor;
import io.github.cbarlin.aru.impl.Constants;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHelper;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;

@Component
@BuilderPerComponentScope
@RequiresProperty(value = "createRemoveMethods", equalTo = "true")
@RequiresBean({CollectionHandlerHelper.class, ConstructorComponent.class, AnalysedCollectionComponent.class})
public final class AddRemover extends CollectionRecordVisitor {

    private final CollectionHandlerHelper minimalCollectionHandler;
    private final BuilderClass builderClass;

    public AddRemover(
        final AnalysedCollectionComponent acc,
        final BuilderClass builderClass,
        final CollectionHandlerHelper minimalCollectionHandler
    ) {
        super(Constants.Claims.BUILDER_REMOVE_SINGLE, acc.parentRecord(), acc);
        this.minimalCollectionHandler = minimalCollectionHandler;
        this.builderClass = builderClass;
    }

    @Override
    public int collectionSpecificity() {
        return 5;
    }

    @Override
    protected boolean visitCollectionComponent() {
        final String methodName = removeNameMethodName();
        final MethodSpec.Builder method = builderClass.createMethod(methodName, claimableOperation, analysedCollectionComponent.element());
        final String name = analysedCollectionComponent.name();
        final TypeName innerType = analysedCollectionComponent.unNestedPrimaryTypeName();

        final ParameterSpec param = ParameterSpec.builder(innerType, name, Modifier.FINAL)
            .addJavadoc("A singular instance to be removed from the collection")
            .addAnnotation(NULLABLE)
            .build();

        method.addJavadoc("Remove a singular {@link $T} to the collection for the field {@code $L}", innerType, name)
            .returns(builderClass.className())
            .addParameter(param)
            .addAnnotation(NON_NULL)
            .addModifiers(Modifier.PUBLIC);

        minimalCollectionHandler.writeRemoveSingle(method);

        method.addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(method, this);

        return true;
    }
}
