package io.github.cbarlin.aru.impl.types.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class CollectionHandlerHolder {

    public static final List<CollectionHandler> COLLECTION_HANDLERS;

    static {
        final List<CollectionHandler> handlers = new ArrayList<>();
        ServiceLoader.load(CollectionHandler.class, CollectionHandler.class.getClassLoader())
            .iterator()
            .forEachRemaining(handlers::add);
        COLLECTION_HANDLERS = List.copyOf(handlers);
    }

    private CollectionHandlerHolder() {
        throw new UnsupportedOperationException("Cannot instantiate this as it is a utility class!");
    }
}
