package io.github.cbarlin.aru.impl.types.collection.list;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.ARRAY_LIST;

import io.avaje.spi.ServiceProvider;

@ServiceProvider
public final class ArrayListCollectionHandler extends ListCollectionHandler {

    public ArrayListCollectionHandler() {
        super(ARRAY_LIST, ARRAY_LIST);
    }
}
