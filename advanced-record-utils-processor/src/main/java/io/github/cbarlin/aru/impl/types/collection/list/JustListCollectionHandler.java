package io.github.cbarlin.aru.impl.types.collection.list;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.ARRAY_LIST;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.LIST;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@Component
@GlobalScope
public final class JustListCollectionHandler extends ListCollectionHandler {

    public JustListCollectionHandler() {
        super(LIST, ARRAY_LIST);
    }

    @Override
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String targetVariableName, final TypeName innerTypeName) {
        methodBuilder.addStatement(
            "final $T<$T> $L = $L.stream()\n    .filter($T::nonNull)\n    .toList()",
            immutableClassName,
            innerTypeName,
            targetVariableName,
            fieldName,
            OBJECTS
        );
    }
}
