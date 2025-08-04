package io.github.cbarlin.aru.impl.builder.collection;

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

@Component
@BuilderPerComponentScope
@RequiresBean({CollectionHandlerHelper.class, AnalysedCollectionComponent.class, ConstructorComponent.class})
public final class AddGetter extends CollectionRecordVisitor {

    private final CollectionHandlerHelper minimalCollectionHandler;
    private final BuilderClass builderClass;

    public AddGetter(
        final AnalysedCollectionComponent acc,
        final BuilderClass builderClass,
        final CollectionHandlerHelper minimalCollectionHandler
    ) {
        super(CommonsConstants.Claims.CORE_BUILDER_GETTER, acc.parentRecord(), acc);
        this.minimalCollectionHandler = minimalCollectionHandler;
        this.builderClass = builderClass;
    }

    @Override
    public int collectionSpecificity() {
        return 5;
    }

    @Override
    protected boolean visitCollectionComponent() {
        final MethodSpec.Builder method = builderClass
            .createMethod(analysedCollectionComponent.name(), claimableOperation, analysedCollectionComponent.element());

        minimalCollectionHandler.writeGetter(method);
        AnnotationSupplier.addGeneratedAnnotation(method, this);
        return true;
    }
}
