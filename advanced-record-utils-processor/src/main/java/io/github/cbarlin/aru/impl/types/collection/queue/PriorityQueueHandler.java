package io.github.cbarlin.aru.impl.types.collection.queue;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.ClassName;

@Component
@GlobalScope
public final class PriorityQueueHandler extends QueueCollectionHandler {
    private static final ClassName CLASS_NAME = ClassName.get("java.util", "PriorityQueue");

    PriorityQueueHandler() {
        super(CLASS_NAME, CLASS_NAME, CLASS_NAME);
    }

}
