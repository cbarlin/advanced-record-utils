package io.github.cbarlin.aru.impl.builder.collection;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;

import javax.lang.model.element.Modifier;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.CollectionRecordVisitor;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHelper;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@Component
@BuilderPerComponentScope
@RequiresProperty(value = "createAdderMethods", equalTo = "true")
@RequiresBean({CollectionHandlerHelper.class, ConstructorComponent.class, AnalysedCollectionComponent.class})
public final class AddAdder extends CollectionRecordVisitor {

    private final CollectionHandlerHelper minimalCollectionHandler;
    private final BuilderClass builderClass;

    public AddAdder(
        final AnalysedCollectionComponent acc,
        final BuilderClass builderClass,
        final CollectionHandlerHelper minimalCollectionHandler
    ) {
        super(Claims.CORE_BUILDER_SINGLE_ITEM_ADDER, acc.parentRecord(), acc);
        this.minimalCollectionHandler = minimalCollectionHandler;
        this.builderClass = builderClass;
    }

    @Override
    public int collectionSpecificity() {
        return 5;
    }

    @Override
    protected boolean visitCollectionComponent() {
        final String methodName = addNameMethodName();
        final MethodSpec.Builder method = builderClass.createMethod(methodName, claimableOperation, analysedCollectionComponent.element());
        final String name = analysedCollectionComponent.name();
        final TypeName innerType = analysedCollectionComponent.unNestedPrimaryTypeName();

        final ParameterSpec param = ParameterSpec.builder(innerType, name, Modifier.FINAL)
            .addJavadoc("A singular instance to be added to the collection")
            .addAnnotation(NULLABLE)
            .build();

        method.addJavadoc("Add a singular {@link $T} to the collection for the field {@code $L}", innerType, name)
            .returns(builderClass.className())
            .addParameter(param)
            .addAnnotation(CommonsConstants.Names.NON_NULL)
            .addModifiers(Modifier.PUBLIC);

        if (minimalCollectionHandler.nullReplacesNotNull()) {
            method.addJavadoc("\n<p>\n")
                .addJavadoc("Supplying a null value will set the current value to null");
        } else {
            method.addJavadoc("\n<p>\n")
                .addJavadoc("Supplying a null value won't replace a set value");
        }

        minimalCollectionHandler.writeAddSingle(method);

        method.addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(method, this);

        return true;
    }
}
