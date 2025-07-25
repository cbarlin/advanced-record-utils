package io.github.cbarlin.aru.impl.types.collection.list;

import static io.github.cbarlin.aru.impl.Constants.Names.VECTOR;

import io.avaje.spi.ServiceProvider;

@ServiceProvider
public final class VectorCollectionHandler extends ListCollectionHandler {
    public VectorCollectionHandler() {
        super(VECTOR, VECTOR);
    }
}
