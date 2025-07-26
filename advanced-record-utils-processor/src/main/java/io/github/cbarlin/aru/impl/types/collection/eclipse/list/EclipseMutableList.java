package io.github.cbarlin.aru.impl.types.collection.eclipse.list;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__MUTABLE_LIST;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__PROPERTY;

@Component
@GlobalScope
@RequiresProperty(value = ECLIPSE_COLLECTIONS__PROPERTY, equalTo = "true")
public final class EclipseMutableList extends EclipseListCollectionHandler {

    public EclipseMutableList() {
        super(ECLIPSE_COLLECTIONS__MUTABLE_LIST);
    }

    @Override
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        methodBuilder.addStatement("final $T<$T> $L = $L.clone()", mutableClassName, innerTypeName, assignmentName, fieldName);
    }
}
