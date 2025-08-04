package io.github.cbarlin.aru.impl.types.collection.list;

import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.STACK;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.MethodSpec.Builder;
import io.micronaut.sourcegen.javapoet.TypeName;

@Component
@GlobalScope
public final class StackCollectionHandler extends ListCollectionHandler {
    public StackCollectionHandler() {
        super(STACK, STACK);
    }

    @Override
    public void writeNullableAutoAddSingle(AnalysedComponent component, Builder methodBuilder, TypeName innerType) {
        methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
            .addStatement("this.$L = new $T<$T>()", component.name(), mutableClassName, innerType)
            .endControlFlow()
            .addStatement("this.$L.add($L)", component.name(), component.name());
    }
}
