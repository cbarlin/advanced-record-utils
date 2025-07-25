package io.github.cbarlin.aru.impl.types.collection.list;

import static io.github.cbarlin.aru.impl.Constants.Names.LINKED_LIST;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public final class LinkedListCollectionHandler extends ListCollectionHandler {
    public LinkedListCollectionHandler() {
        super(LINKED_LIST, LINKED_LIST);
    }

    @Override
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        methodBuilder.addStatement("final $T<$T> $L = $L", classNameOnComponent, innerTypeName, assignmentName, fieldName);
    }
}
