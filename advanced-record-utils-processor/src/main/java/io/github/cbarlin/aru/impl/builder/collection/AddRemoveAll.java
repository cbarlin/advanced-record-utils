package io.github.cbarlin.aru.impl.builder.collection;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.CollectionRecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHelper;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;

@Component
@BuilderPerComponentScope
@RequiresProperty(value = "createRemoveMethods", equalTo = "true")
@RequiresBean({CollectionHandlerHelper.class, ConstructorComponent.class, AnalysedCollectionComponent.class})
public final class AddRemoveAll extends CollectionRecordVisitor {

    private final CollectionHandlerHelper minimalCollectionHandler;
    private final BuilderClass builderClass;

    public AddRemoveAll(
        final AnalysedCollectionComponent acc,
        final BuilderClass builderClass,
        final CollectionHandlerHelper minimalCollectionHandler
    ) {
        super(Claims.BUILDER_REMOVE_ALL_ITERABLE, acc.parentRecord(), acc);
        this.minimalCollectionHandler = minimalCollectionHandler;
        this.builderClass = builderClass;
    }

    @Override
    protected int collectionSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitCollectionComponent() {
        final String addName = removeNameMethodName();
        minimalCollectionHandler.writeRemoveMany(builderClass, addName, addName, this);
        return true;
    }
}
