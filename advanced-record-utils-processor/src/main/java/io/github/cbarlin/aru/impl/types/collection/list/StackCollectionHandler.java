package io.github.cbarlin.aru.impl.types.collection.list;

import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.STACK;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec.Builder;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public final class StackCollectionHandler extends ListCollectionHandler {
    public StackCollectionHandler() {
        super(STACK, STACK);
    }

    @Override
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        methodBuilder.addStatement("final $T<$T> $L = $L", classNameOnComponent, innerTypeName, assignmentName, fieldName);
    }

    @Override
    public void writeNullableAutoAddSingle(AnalysedComponent component, Builder methodBuilder, TypeName innerType) {
        methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
            .addStatement("this.$L = new $T<$T>()", component.name(), mutableClassName, innerType)
            .endControlFlow()
            .addStatement("this.$L.add($L)", component.name(), component.name());
    }
}
