package io.github.cbarlin.aru.impl.builder;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NOT_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.TypeAliasComponent;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public class AddAliasSetter extends RecordVisitor {

    public AddAliasSetter() {
        super(Claims.BUILDER_ALIAS_SETTER);
    }

    @Override
    public int specificity() {
        return 1;
    }

    @Override
    public boolean isApplicable(final AnalysedRecord analysedRecord) {
        // Only bother if the record has at least one TypeAlias component
        return analysedRecord.components()
            .stream()
            .anyMatch(TypeAliasComponent.class::isInstance);
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        if (analysedComponent instanceof final TypeAliasComponent tAliasComponent) {
            final String name = tAliasComponent.name();
            final MethodSpec.Builder methodBuilder = tAliasComponent.builderArtifact().createMethod(name, claimableOperation);
            AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
            final boolean nullReplacingNonNull = !Boolean.FALSE.equals(tAliasComponent.settings().prism().builderOptions().nullReplacesNotNull());
            methodBuilder.returns(tAliasComponent.builderArtifact().className())
                .addJavadoc("Updates the value of {@code $L}", name)
                .addAnnotation(NON_NULL)
                .addParameter(
                    ParameterSpec.builder(tAliasComponent.serialisedTypeName(), name, Modifier.FINAL)
                        .addAnnotation(
                            nullReplacingNonNull ? NULLABLE : NOT_NULL
                        )
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
        return false;
    }
}
