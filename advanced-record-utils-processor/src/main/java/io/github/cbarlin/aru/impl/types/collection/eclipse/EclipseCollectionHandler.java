package io.github.cbarlin.aru.impl.types.collection.eclipse;

import io.github.cbarlin.aru.impl.types.collection.StandardCollectionHandler;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

public abstract class EclipseCollectionHandler extends StandardCollectionHandler {

    protected EclipseCollectionHandler(ClassName classNameOnComponent, ClassName mutableClassName, ClassName immutableClassName) {
        super(classNameOnComponent, mutableClassName, immutableClassName);
    }

    @Override
    protected void convertToImmutable(MethodSpec.Builder methodBuilder, String fieldName, String assignmentName, final TypeName innerTypeName) {
        methodBuilder.addStatement("final $T<$T> $L = this.$L.toImmutable()", immutableClassName, innerTypeName, assignmentName, fieldName);
    }
}
