package io.github.cbarlin.aru.impl.types.collection.set;

import static io.github.cbarlin.aru.impl.Constants.Names.TREE_SET;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@Component
@GlobalScope
public final class TreeSetCollectionHandler extends SetCollectionHandler {
    public TreeSetCollectionHandler() {
        super(TREE_SET, TREE_SET);
    }

    @Override
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        methodBuilder
            .addComment("Created in $L", this.getClass().getCanonicalName())
            .addStatement("final $T<$T> $L = $L", classNameOnComponent, innerTypeName, assignmentName, fieldName);
    }
}
