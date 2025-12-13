package io.github.cbarlin.aru.impl.builder;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.TypeAliasComponent;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

@Component
@BuilderPerComponentScope
@RequiresBean({ConstructorComponent.class, TypeAliasComponent.class})
public final class AddAliasSetter extends RecordVisitor {

    private final TypeAliasComponent tAliasComponent;
    private final BuilderClass builderClass;

    public AddAliasSetter(
        final TypeAliasComponent typeAliasComponent, 
        final BuilderClass builderClass
    ) {
        super(Claims.BUILDER_ALIAS_SETTER, typeAliasComponent.parentRecord());
        this.tAliasComponent = typeAliasComponent;
        this.builderClass = builderClass;
    }

    @Override
    public int specificity() {
        return 1;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final String name = tAliasComponent.name();
        final MethodSpec.Builder methodBuilder = builderClass.createMethod(name, claimableOperation);
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        final boolean nullReplacingNonNull = !Boolean.FALSE.equals(settings.prism().builderOptions().nullReplacesNotNull());

        methodBuilder.returns(builderClass.className())
            .addJavadoc("Updates the value of {@code $L}", name)
            .addParameter(
                ParameterSpec.builder(tAliasComponent.serialisedTypeName().annotated(CommonsConstants.NULLABLE_ANNOTATION), name, Modifier.FINAL)
                    .addJavadoc("The replacement value")
                    .build()
            );
        if (nullReplacingNonNull) {
            methodBuilder.beginControlFlow("if ($T.isNull($L))", OBJECTS, name)
                .addStatement("this.$L = null", name)
                .addStatement("return this")
                .endControlFlow();
        } else {
            methodBuilder.beginControlFlow("if ($T.isNull($L))", OBJECTS, name)
                .addStatement("return this")
                .endControlFlow();
        }
        methodBuilder.addStatement("return this.$L(new $T($L))", name, tAliasComponent.typeName(), name);
        return true;
    }
}
