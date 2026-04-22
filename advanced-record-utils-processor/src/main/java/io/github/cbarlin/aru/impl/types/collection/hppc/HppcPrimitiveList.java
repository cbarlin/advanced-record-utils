package io.github.cbarlin.aru.impl.types.collection.hppc;

import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.List;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.MATH;

public final class HppcPrimitiveList extends AbstractHppcPrimitiveHandler {

    private final ClassName setName;
    private final ClassName mapToLongName;
    private final ClassName cursorName;

    public HppcPrimitiveList(
        final List<ClassName> possibleClassNames,
        final ClassName concreteClassName,
        final ClassName setName,
        final ClassName mapToLongName,
        final ClassName cursorName
    ) {
        super(possibleClassNames, concreteClassName);
        this.setName = setName;
        this.mapToLongName = mapToLongName;
        this.cursorName = cursorName;
    }

    @Override
    public void writeDifferMethod(final TypeName innerType, final MethodSpec.Builder methodBuilder, final ClassName collectionResultRecord) {
        methodBuilder.addModifiers(Modifier.FINAL, Modifier.STATIC)
                .returns(collectionResultRecord)
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
                .addStatement("final $T nUpdated = $T.requireNonNullElse(updated, new $T())", concreteClassName, OBJECTS, concreteClassName)
                .addStatement("final $T nOriginal = $T.requireNonNullElse(original, new $T())", concreteClassName, OBJECTS, concreteClassName)
                .addComment("Create frequency maps to count occurrences")
                .addStatement(
                        """
                        final $T originalFreq = new $T()
                        """.trim(),
                        mapToLongName,
                        mapToLongName
                )
                .addStatement(
                        """
                        final $T updatedFreq = new $T()
                        """.trim(),
                        mapToLongName,
                        mapToLongName
                )
                .beginControlFlow("for (final $T element : nOriginal)", cursorName)
                .addStatement("originalFreq.addTo(element.value, 1l)")
                .endControlFlow()
                .beginControlFlow("for (final $T element : nUpdated)", cursorName)
                .addStatement("updatedFreq.addTo(element.value, 1l)")
                .endControlFlow()
                .addStatement("final $T added = new $T()", concreteClassName, concreteClassName)
                .addStatement("final $T removed = new $T()", concreteClassName, concreteClassName)
                .addStatement("final $T common = new $T()", concreteClassName, concreteClassName)
                .addComment("Obtain unique elements")
                .addStatement("final $T allUniqueElements = new $T(originalFreq.keys())", setName, setName)
                .addStatement("allUniqueElements.addAll(updatedFreq.keys())")
                .beginControlFlow("for (final $T el : allUniqueElements)", cursorName)
                .addStatement("final $T element = el.value", innerType)
                .addStatement("final long originalCount = originalFreq.getOrDefault(element, 0L)")
                .addStatement("final long updatedCount = updatedFreq.getOrDefault(element, 0L)")
                .addStatement("final long commonCount = $T.min(originalCount, updatedCount)", MATH)

                .beginControlFlow("for (long i = 0; i < commonCount; i++)")
                .addStatement("common.add(element)")
                .endControlFlow()

                .beginControlFlow("if (originalCount > updatedCount)")
                .addStatement("final long removedCount = originalCount - updatedCount")
                .beginControlFlow("for (long i = 0; i < removedCount; i++)")
                .addStatement("removed.add(element)")
                .endControlFlow()
                .endControlFlow()

                .beginControlFlow("if (updatedCount > originalCount)")
                .addStatement("final long addedCount = updatedCount - originalCount")
                .beginControlFlow("for (long i = 0; i < addedCount; i++)")
                .addStatement("added.add(element)")
                .endControlFlow()
                .endControlFlow()

                .endControlFlow()
                .addStatement(
                        "return new $T(added, common, removed)",
                        collectionResultRecord
                );
    }
}
