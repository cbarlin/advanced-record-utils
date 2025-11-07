package io.github.cbarlin.aru.impl.types.collection.set;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.HASH_SET;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@Component
@GlobalScope
public final class HashSetCollectionHandler extends SetCollectionHandler {

    public HashSetCollectionHandler() {
        super(HASH_SET, HASH_SET);
    }

    @Override
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        methodBuilder
            .addComment("No immutable version exists - returning original object")
            .addStatement("final $T<$T> $L = $L", classNameOnComponent, innerTypeName, assignmentName, fieldName);
    }
}
