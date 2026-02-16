package io.github.cbarlin.aru.impl.builder.map;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants;
import io.github.cbarlin.aru.impl.types.AnalysedMapComponent;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

@Component
@BuilderPerComponentScope
@RequiresBean({ConstructorComponent.class, AnalysedMapComponent.class})
public final class MapGet extends RecordVisitor {

    private static final String TO_UNMODIFIABLE_COLLECTION = """
return this.$L.entrySet()
    .stream()
    .filter($T::nonNull)
    .filter(e -> $T.nonNull(e.getKey()) && $T.nonNull(e.getValue()))
    .collect($T.toUnmodifiableMap($T::getKey, $T::getValue))""";

    private final BuilderClass builderClass;
    private final AnalysedMapComponent analysedMapComponent;

    public MapGet(
            final AnalysedRecord analysedRecord,
            final BuilderClass builderClass,
            final AnalysedMapComponent analysedMapComponent
    ) {
        super(CommonsConstants.Claims.CORE_BUILDER_GETTER, analysedRecord);
        this.builderClass = builderClass;
        this.analysedMapComponent = analysedMapComponent;
    }

    @Override
    public int specificity() {
        return 3;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final boolean immutable = !"AUTO".equals(analysedRecord.settings().prism().builderOptions().builtCollectionType());
        final boolean buildEmpty = !Boolean.FALSE.equals(analysedRecord.settings().prism().builderOptions().buildNullCollectionToEmpty());
        final String fieldName = analysedMapComponent.name();
        final boolean isPlainMap = "Map".equals(analysedMapComponent.erasedMapTypeName().simpleName());
        if (immutable && isPlainMap) {
            final MethodSpec.Builder method = builderClass
                    .createMethod(analysedMapComponent.name(), claimableOperation, analysedMapComponent.element())
                    .returns(analysedMapComponent.mapTypeName())
                    .addJavadoc("Returns the current value of {@code $L}", fieldName);

            if (buildEmpty) {
                method.beginControlFlow("if ($T.isNull(this.$L) || this.$L.isEmpty())", OBJECTS, fieldName, fieldName)
                        .addStatement("return $T.of()", Constants.Names.MAP)
                        .endControlFlow()
                        .addAnnotation(CommonsConstants.NON_NULL_ANNOTATION);
            } else {
                method.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, fieldName)
                        .addStatement("return null", Constants.Names.MAP)
                        .endControlFlow()
                        .addAnnotation(CommonsConstants.NULLABLE_ANNOTATION);
            }
            method.addStatement(
                    TO_UNMODIFIABLE_COLLECTION,
                    fieldName,
                    OBJECTS,
                    OBJECTS,
                    OBJECTS,
                    CommonsConstants.Names.COLLECTORS,
                    Constants.Names.MAP_ENTRY,
                    Constants.Names.MAP_ENTRY
            );
            AnnotationSupplier.addGeneratedAnnotation(method, this);
            return true;
        } else if (buildEmpty) {
            final MethodSpec.Builder method = builderClass
                    .createMethod(analysedMapComponent.name(), claimableOperation, analysedMapComponent.element())
                    .returns(analysedMapComponent.mapTypeName())
                    .addJavadoc("Returns the current value of {@code $L}", fieldName);
            method.beginControlFlow("if ($T.isNull(this.$L) || this.$L.isEmpty())", OBJECTS, fieldName, fieldName);
            if (isPlainMap) {
                method.addStatement("return $T.of()", Constants.Names.MAP);
            } else {
                method.addStatement("return new $T<>()", analysedMapComponent.mutableTypeName());
            }
            method.endControlFlow()
                    .addStatement("return this.$L", fieldName);
            AnnotationSupplier.addGeneratedAnnotation(method, this);
            return true;
        }
        return false;
    }
}
