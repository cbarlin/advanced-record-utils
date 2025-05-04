package io.github.cbarlin.aru.impl.builder;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.TypeAliasComponent;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

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
        return true;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        if (analysedComponent instanceof final TypeAliasComponent tAliasComponent) {
            final String name = tAliasComponent.name();
            final MethodSpec.Builder methodBuilder = analysedComponent.builderArtifact().createMethod(name, claimableOperation);
            AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
            methodBuilder.returns(tAliasComponent.builderArtifact().className())
                .addJavadoc("Updates the value of {@code $L}", name)
                .addAnnotation(NON_NULL)
                .addParameter(
                    ParameterSpec.builder(tAliasComponent.serialisedTypeName(), name, Modifier.FINAL)
                        .addJavadoc("The replacement value")
                        .build()
                )
                .addStatement("return this.$L(new $T($L))", name, tAliasComponent.typeName(), name);
            return true;
        }
        return false;
    }
}
