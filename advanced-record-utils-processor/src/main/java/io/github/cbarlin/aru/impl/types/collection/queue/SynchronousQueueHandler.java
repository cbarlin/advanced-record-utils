package io.github.cbarlin.aru.impl.types.collection.queue;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.ClassName;

@Component
@GlobalScope
public final class SynchronousQueueHandler extends QueueCollectionHandler {
    private static final ClassName CLASS_NAME = ClassName.get("java.util.concurrent", "SynchronousQueue");

    SynchronousQueueHandler() {
        super(CLASS_NAME, CLASS_NAME, CLASS_NAME);
    }
}
