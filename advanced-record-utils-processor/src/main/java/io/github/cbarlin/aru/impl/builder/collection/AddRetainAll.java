package io.github.cbarlin.aru.impl.builder.collection;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.CollectionRecordVisitor;
import io.github.cbarlin.aru.impl.Constants;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHelper;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

@Component
@BuilderPerComponentScope
@RequiresProperty(value = "createRetainAllMethod", equalTo = "true")
@RequiresBean({CollectionHandlerHelper.class, ConstructorComponent.class, AnalysedCollectionComponent.class})
public final class AddRetainAll extends CollectionRecordVisitor {

    private final CollectionHandlerHelper minimalCollectionHandler;
    private final BuilderClass builderClass;

    public AddRetainAll(
        final AnalysedCollectionComponent acc,
        final BuilderClass builderClass,
        final CollectionHandlerHelper minimalCollectionHandler
    ) {
        super(Constants.Claims.BUILDER_RETAIN_ALL, acc.parentRecord(), acc);
        this.minimalCollectionHandler = minimalCollectionHandler;
        this.builderClass = builderClass;
    }

    @Override
    public int collectionSpecificity() {
        return 5;
    }

    @Override
    protected boolean visitCollectionComponent() {
        final String methodName = analysedCollectionComponent.settings().prism().builderOptions().retainMethodPrefix() +
            capitalise(analysedCollectionComponent.name()) +
            analysedCollectionComponent.settings().prism().builderOptions().retainMethodSuffix();
        final MethodSpec.Builder method = builderClass.createMethod(methodName, claimableOperation, analysedCollectionComponent.element());
        final String name = analysedCollectionComponent.name();
        final TypeName innerType = analysedCollectionComponent.unNestedPrimaryTypeName();
        final ParameterizedTypeName ptn = ParameterizedTypeName.get(CommonsConstants.Names.COLLECTION, innerType);

        final ParameterSpec param = ParameterSpec.builder(ptn, name, Modifier.FINAL)
            .addJavadoc("The collection of items to be retained in the collection")
            .addAnnotation(NON_NULL)
            .build();

        method.addJavadoc("Retain only {@link $T} items from the {@code $L} collection if they are present in the provided collection", innerType, name)
            .returns(builderClass.className())
            .addParameter(param)
            .addAnnotation(NON_NULL)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("$T.requireNonNull($L, $S)", OBJECTS, name, "Cannot provide a null collection");

        minimalCollectionHandler.writeRetainAll(method);

        method.addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(method, this);

        return true;
    }
}
