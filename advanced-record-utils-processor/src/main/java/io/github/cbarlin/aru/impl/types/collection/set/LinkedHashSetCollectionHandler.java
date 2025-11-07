package io.github.cbarlin.aru.impl.types.collection.set;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@Component
@GlobalScope
public final class LinkedHashSetCollectionHandler extends SetCollectionHandler {
    private static final ClassName CLASS_NAME = ClassName.get("java.util", "LinkedHashSet");

    public LinkedHashSetCollectionHandler() {
        super(CLASS_NAME, CLASS_NAME);
    }

    @Override
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        methodBuilder
            .addComment("No immutable version exists - returning original object")
            .addStatement("final $T<$T> $L = $L", classNameOnComponent, innerTypeName, assignmentName, fieldName);
    }
}
