package io.github.cbarlin.aru.impl.types.collection.list;

import static io.github.cbarlin.aru.impl.Constants.Names.LINKED_LIST;

import io.avaje.spi.ServiceProvider;

@ServiceProvider
public final class LinkedListCollectionHandler extends ListCollectionHandler {
    public LinkedListCollectionHandler() {
        super(LINKED_LIST, LINKED_LIST);
    }
}
