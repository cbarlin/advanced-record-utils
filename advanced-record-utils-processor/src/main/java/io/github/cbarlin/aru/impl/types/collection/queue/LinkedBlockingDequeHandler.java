package io.github.cbarlin.aru.impl.types.collection.queue;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.ClassName;

@Component
@GlobalScope
public final class LinkedBlockingDequeHandler extends QueueCollectionHandler {
    private static final ClassName CLASS_NAME = ClassName.get("java.util.concurrent", "LinkedBlockingDeque");

    LinkedBlockingDequeHandler() {
        super(CLASS_NAME, CLASS_NAME, CLASS_NAME);
    }

}
