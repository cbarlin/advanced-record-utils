package io.github.cbarlin.aru.impl.types;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;

/**
 * A subtype of {@link AnalysedCollectionComponent} that is a primitive collection type
 */
public abstract class AnalysedPrimitiveCollectionComponent extends AnalysedCollectionComponent {
    public AnalysedPrimitiveCollectionComponent(
        final AnalysedComponent delegate,
        final TypeMirror innerType,
        final TypeName innerTypeName,
        final ClassName erasedWrapperClassName
    ) {
        super(delegate, innerType, innerTypeName, erasedWrapperClassName);
    }
}
