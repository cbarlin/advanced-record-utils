package io.github.cbarlin.aru.impl.types.collection.list;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.ARRAY_LIST;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public final class ArrayListCollectionHandler extends ListCollectionHandler {

    public ArrayListCollectionHandler() {
        super(ARRAY_LIST, ARRAY_LIST);
    }

    @Override
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        methodBuilder.addStatement("final $T<$T> $L = $L", classNameOnComponent, innerTypeName, assignmentName, fieldName);
    }
}
