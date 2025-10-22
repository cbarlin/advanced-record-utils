package io.github.cbarlin.aru.impl.types.collection.queue;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;

import static io.github.cbarlin.aru.impl.Constants.Names.DEQUE;
import static io.github.cbarlin.aru.impl.Constants.Names.LINKED_LIST;

@Component
@GlobalScope
public final class JustDequeCollectionHandler extends QueueCollectionHandler {
    JustDequeCollectionHandler() {
        super(DEQUE, LINKED_LIST, DEQUE);
    }
}
