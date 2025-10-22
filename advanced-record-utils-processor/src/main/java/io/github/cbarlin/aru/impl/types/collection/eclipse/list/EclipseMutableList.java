package io.github.cbarlin.aru.impl.types.collection.eclipse.list;

import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__MUTABLE_LIST;

public final class EclipseMutableList extends EclipseListCollectionHandler {

    public EclipseMutableList() {
        super(ECLIPSE_COLLECTIONS__MUTABLE_LIST);
    }

    @Override
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        methodBuilder
            .addComment("Created in $L", this.getClass().getCanonicalName())
            .addStatement("final $T<$T> $L = $L.clone()", mutableClassName, innerTypeName, assignmentName, fieldName);
    }
}
