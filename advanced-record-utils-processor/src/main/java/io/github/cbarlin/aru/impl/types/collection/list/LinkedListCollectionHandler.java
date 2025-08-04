package io.github.cbarlin.aru.impl.types.collection.list;

import static io.github.cbarlin.aru.impl.Constants.Names.LINKED_LIST;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;

@Component
@GlobalScope
public final class LinkedListCollectionHandler extends ListCollectionHandler {
    public LinkedListCollectionHandler() {
        super(LINKED_LIST, LINKED_LIST);
    }
}
