package io.github.cbarlin.aru.impl.types.collection.eclipse.set;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__IMMUTABLE_SET;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__PROPERTY;

@Component
@GlobalScope
@RequiresProperty(value = ECLIPSE_COLLECTIONS__PROPERTY, equalTo = "true")
public final class EclipseImmutableSet extends EclipseSetCollectionHandler {

    public EclipseImmutableSet() {
        super(ECLIPSE_COLLECTIONS__IMMUTABLE_SET);
    }

    @Override
    public void writeMergerMethod(TypeName innerType, MethodSpec.Builder methodBuilder) {
        final ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(classNameOnComponent, innerType);
        final ParameterSpec paramA = ParameterSpec.builder(paramTypeName, "elA", Modifier.FINAL)
            .addAnnotation(NULLABLE)
            .addJavadoc("The preferred input")
            .build();

        final ParameterSpec paramB = ParameterSpec.builder(paramTypeName, "elB", Modifier.FINAL)
            .addAnnotation(NULLABLE)
            .addJavadoc("The non-preferred input")
            .build();
        methodBuilder.addAnnotation(NULLABLE)
            .addParameter(paramA)
            .addParameter(paramB)
            .returns(paramTypeName)
            .addJavadoc("Merger for fields of class {@link $T}", paramTypeName)
            .beginControlFlow("if ($T.isNull(elA) || elA.isEmpty())", OBJECTS)
            .addStatement("return elB")
            .nextControlFlow("else if ($T.isNull(elB) || elB.isEmpty())", OBJECTS)
            .addStatement("return elA")
            .endControlFlow()
            .addStatement("return elA.newWithAll(elB)");
    }
}
