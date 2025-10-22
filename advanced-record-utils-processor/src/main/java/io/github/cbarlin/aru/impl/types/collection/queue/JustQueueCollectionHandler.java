package io.github.cbarlin.aru.impl.types.collection.queue;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.*;
import static io.github.cbarlin.aru.impl.Constants.Names.*;

@Component
@GlobalScope
public final class JustQueueCollectionHandler extends QueueCollectionHandler {
    JustQueueCollectionHandler() {
        super(QUEUE, LINKED_LIST, QUEUE);
    }
}
