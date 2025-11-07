package io.github.cbarlin.aru.impl.types.collection.eclipse.set;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__IMMUTABLE_SET;


public final class EclipseImmutableSet extends EclipseSetCollectionHandler {

    public EclipseImmutableSet() {
        super(ECLIPSE_COLLECTIONS__IMMUTABLE_SET);
    }

    @Override
    public void writeMergerMethod(final TypeName innerType, final MethodSpec.Builder methodBuilder) {
        final ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(classNameOnComponent, innerType);
        writeMergerMethodImpl(methodBuilder, paramTypeName);
    }

    @Override
    public void writeNonNullAutoGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        // The field in the builder is also NonNull in this case, so no need for null check
        methodBuilder.addStatement("return this.$L.toImmutable()", component.name())
                .returns(component.typeName());
    }
}
