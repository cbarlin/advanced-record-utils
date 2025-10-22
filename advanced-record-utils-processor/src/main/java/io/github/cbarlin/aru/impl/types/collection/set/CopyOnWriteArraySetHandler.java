package io.github.cbarlin.aru.impl.types.collection.set;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@Component
@GlobalScope
public final class CopyOnWriteArraySetHandler extends SetCollectionHandler {
    private static final ClassName CLASS_NAME = ClassName.get("java.util.concurrent", "CopyOnWriteArraySet");

    public CopyOnWriteArraySetHandler() {
        super(CLASS_NAME, CLASS_NAME);
    }

    @Override
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        methodBuilder
            .addComment("Created in $L", this.getClass().getCanonicalName())
            .addStatement("final $T<$T> $L = $L", classNameOnComponent, innerTypeName, assignmentName, fieldName);
    }
}
