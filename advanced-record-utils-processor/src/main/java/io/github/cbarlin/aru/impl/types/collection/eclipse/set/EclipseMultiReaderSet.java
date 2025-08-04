package io.github.cbarlin.aru.impl.types.collection.eclipse.set;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__MULTIREADER_SET;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__PROPERTY;

@Component
@GlobalScope
@RequiresProperty(value = ECLIPSE_COLLECTIONS__PROPERTY, equalTo = "true")
public final class EclipseMultiReaderSet extends EclipseSetCollectionHandler {
    public EclipseMultiReaderSet() {
        super(ECLIPSE_COLLECTIONS__MULTIREADER_SET);
    }

    @Override
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        methodBuilder.addStatement("final $T<$T> $L = $L.clone()", mutableClassName, innerTypeName, assignmentName, fieldName);
    }
}
