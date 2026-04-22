package io.github.cbarlin.aru.impl.types.collection.hppc;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.List;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

public final class HppcPrimitiveSet extends AbstractHppcPrimitiveHandler {

    private final ClassName cursorName;

    public HppcPrimitiveSet(final List<ClassName> possibleClassNames, final ClassName concreteClassName, final ClassName cursorName) {
        super(possibleClassNames, concreteClassName);
        this.cursorName = cursorName;
    }

    @Override
    public void writeNonNullAutoRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        final String name = component.name();
        methodBuilder.addStatement("this.$L.remove($L)", name, name);
    }

    @Override
    public void writeNullableAutoRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        methodBuilder.beginControlFlow("if (this.$L == null)", component.name())
                .addStatement("this.$L = new $T()", component.name(), concreteClassName)
                .endControlFlow()
                .addStatement("this.$L.remove($L)", component.name(), component.name());
    }

    @Override
    public void writeDifferMethod(final TypeName innerType, final MethodSpec.Builder methodBuilder, final ClassName collectionResultRecord) {
        methodBuilder
                .addParameter(
                        ParameterSpec.builder(concreteClassName, "original", Modifier.FINAL)
                                .addAnnotation(NULLABLE)
                                .build()
                )
                .addParameter(
                        ParameterSpec.builder(concreteClassName, "updated", Modifier.FINAL)
                                .addAnnotation(NULLABLE)
                                .build()
                )
                .returns(collectionResultRecord)
                .addComment("Filter elements by comparing against the other set")
                .addStatement("final $T nUpdated = $T.requireNonNullElse(updated, new $T())", concreteClassName, OBJECTS, concreteClassName)
                .addStatement("final $T nOriginal = $T.requireNonNullElse(original, new $T())", concreteClassName, OBJECTS, concreteClassName)
                .addStatement("final $T added = new $T()", concreteClassName, concreteClassName)
                .addStatement("final $T removed = new $T()", concreteClassName, concreteClassName)
                .addStatement("final $T common = new $T()", concreteClassName, concreteClassName)
                .beginControlFlow("for ($T el : nOriginal)", cursorName)
                .addStatement("(nUpdated.contains(el.value) ? common : removed).add(el.value)")
                .endControlFlow()
                .beginControlFlow("for ($T el : nUpdated)", cursorName)
                .beginControlFlow("if (!nOriginal.contains(el.value))")
                .addStatement("added.add(el.value)")
                .endControlFlow()
                .endControlFlow();
        // Constructor is added, common, removed
        methodBuilder.addStatement("return new $T(added, common, removed)", collectionResultRecord);
    }
}
