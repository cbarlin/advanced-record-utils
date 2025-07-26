package io.github.cbarlin.aru.impl.types.collection.eclipse;

import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.types.collection.StandardCollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.eclipse.list.EclipseListCollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.eclipse.set.EclipseSetCollectionHandler;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.NON_NULL;

public sealed abstract class EclipseCollectionHandler extends StandardCollectionHandler permits EclipseListCollectionHandler, EclipseSetCollectionHandler {

    protected final ClassName factoryClassName;

    protected EclipseCollectionHandler(
        final ClassName classNameOnComponent,
        final ClassName mutableClassName,
        final ClassName immutableClassName,
        final ClassName factoryClassName
    ) {
        super(classNameOnComponent, mutableClassName, immutableClassName);
        this.factoryClassName = factoryClassName;
    }

    @Override
    public void writeNonNullAutoSetter(AnalysedComponent component, MethodSpec.Builder methodBuilder, TypeName innerType, boolean nullReplacesNonNull) {
        if (nullReplacesNonNull) {
            methodBuilder.addStatement("this.$L.clear()", component.name())
                    .beginControlFlow("if ($T.nonNull($L))", OBJECTS, component.name())
                    .addStatement("this.$L.addAllIterable($L)", component.name(), component.name())
                    .endControlFlow();
        } else {
            methodBuilder.beginControlFlow("if ($T.nonNull($L))", OBJECTS, component.name())
                    .addStatement("this.$L.clear()", component.name())
                    .addStatement("this.$L.addAllIterable($L)", component.name(), component.name())
                    .endControlFlow();
        }
    }

    @Override
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        methodBuilder.addStatement("final $T<$T> $L = $L.toImmutable()", immutableClassName, innerTypeName, assignmentName, fieldName);
    }

    @Override
    public void addNonNullAutoField(final AnalysedComponent ecc, final ToBeBuilt addFieldTo, final TypeName innerType) {
        final ParameterizedTypeName ptn = ParameterizedTypeName.get(mutableClassName, innerType);
        final FieldSpec fSpec = FieldSpec.builder(ptn, ecc.name(), Modifier.PRIVATE)
            .addAnnotation(NON_NULL)
            .initializer("$T.mutable.empty()", factoryClassName)
            .build();
        addFieldTo.addField(fSpec);
    }
}
