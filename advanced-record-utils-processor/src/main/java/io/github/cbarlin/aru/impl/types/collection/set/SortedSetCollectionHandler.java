package io.github.cbarlin.aru.impl.types.collection.set;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.COLLECTIONS;
import static io.github.cbarlin.aru.impl.Constants.Names.SORTED_SET;
import static io.github.cbarlin.aru.impl.Constants.Names.TREE_SET;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec.Builder;
import io.micronaut.sourcegen.javapoet.TypeName;

@Component
@GlobalScope
public final class SortedSetCollectionHandler extends SetCollectionHandler {
    public SortedSetCollectionHandler() {
        super(SORTED_SET, TREE_SET);
    }

    @Override
    public void writeNonNullAutoGetter(final AnalysedComponent component, final Builder methodBuilder, final TypeName innerType) {
        methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
            .addStatement("return new $T<>()", TREE_SET)
            .endControlFlow()
            .addStatement("return this.$L", component.name())
            .returns(component.typeName());
    }

    @Override
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        methodBuilder
            .addComment("Created in $L", this.getClass().getCanonicalName())
            .addStatement("final $T<$T> $L = $T.unmodifiableSortedSet($L)", SORTED_SET, innerTypeName, assignmentName, COLLECTIONS, fieldName);
    }

    @Override
    public void writeNonNullImmutableGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
            .addStatement("return $T.emptySortedSet()", COLLECTIONS)
            .endControlFlow()
            .addStatement("return this.$L", component.name())
            .returns(component.typeName());
    }
}
