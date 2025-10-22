package io.github.cbarlin.aru.impl.types.collection.list;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.ClassName;

@Component
@GlobalScope
public final class CopyOnWriteArrayListHandler extends ListCollectionHandler {

    private static final ClassName CLASS_NAME = ClassName.get("java.util.concurrent", "CopyOnWriteArrayList");

    public CopyOnWriteArrayListHandler() {
        super(CLASS_NAME, CLASS_NAME);
    }
}
