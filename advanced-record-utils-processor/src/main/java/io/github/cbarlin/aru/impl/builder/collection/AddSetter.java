package io.github.cbarlin.aru.impl.builder.collection;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;

import javax.lang.model.element.Modifier;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.CollectionRecordVisitor;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHelper;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@Component
@BuilderPerComponentScope
@RequiresBean({CollectionHandlerHelper.class, AnalysedCollectionComponent.class, ConstructorComponent.class})
public final class AddSetter extends CollectionRecordVisitor {

    private final CollectionHandlerHelper minimalCollectionHandler;
    private final BuilderClass builderClass;

    public AddSetter(
        final AnalysedCollectionComponent acc,
        final BuilderClass builderClass,
        final CollectionHandlerHelper minimalCollectionHandler
    ) {
        super(CommonsConstants.Claims.CORE_BUILDER_SETTER, acc.parentRecord(), acc);
        this.minimalCollectionHandler = minimalCollectionHandler;
        this.builderClass = builderClass;
    }

    @Override
    public int collectionSpecificity() {
        return 5;
    }

    @Override
    protected boolean visitCollectionComponent() {
        final String name = analysedCollectionComponent.name();
        final MethodSpec.Builder method = builderClass
            .createMethod(name, claimableOperation, analysedCollectionComponent.element());

        populateStartOfMethod(analysedCollectionComponent, method, minimalCollectionHandler.nullReplacesNotNull(), name);
        minimalCollectionHandler.writeSetter(method);

        method.addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(method, this);

        return true;
    }

    private void populateStartOfMethod(final AnalysedCollectionComponent ac, final MethodSpec.Builder method, final boolean nullReplacesNotNull, final String name) {
        final ParameterSpec param = ParameterSpec.builder(ac.typeName(), name, Modifier.FINAL)
            .addJavadoc("The replacement value")
            .addAnnotation(NULLABLE)
            .build();

        method.addJavadoc("Updates the value of {@code $L}", name)
            .returns(builderClass.className())
            .addParameter(param)
            .addAnnotation(CommonsConstants.Names.NON_NULL)
            .addModifiers(Modifier.PUBLIC);
        
        if (nullReplacesNotNull) {
            method.addJavadoc("\n<p>\n")
                .addJavadoc("Supplying a null value will set the current value to null/empty");
        } else {
            method.addJavadoc("\n<p>\n")
                .addJavadoc("Supplying a null value results in a no-op");
        }
    }
}
