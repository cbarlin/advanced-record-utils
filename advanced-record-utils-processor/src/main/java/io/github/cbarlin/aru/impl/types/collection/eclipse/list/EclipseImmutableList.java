package io.github.cbarlin.aru.impl.types.collection.eclipse.list;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;

import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__IMMUTABLE_LIST;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__PROPERTY;

@Component
@GlobalScope
@RequiresProperty(value = ECLIPSE_COLLECTIONS__PROPERTY, equalTo = "true")
public final class EclipseImmutableList extends EclipseListCollectionHandler {

    public EclipseImmutableList() {
        super(ECLIPSE_COLLECTIONS__IMMUTABLE_LIST);
    }

}
