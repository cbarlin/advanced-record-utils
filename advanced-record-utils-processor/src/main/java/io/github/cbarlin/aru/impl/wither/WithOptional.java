package io.github.cbarlin.aru.impl.wither;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.impl.types.AnalysedOptionalComponent;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public final class WithOptional extends WitherVisitor {

    public WithOptional() {
        super(Claims.WITHER_WITH_OPTIONAL);
    }

    @Override
    protected int witherSpecificity() {
        return 1;
    }

    @Override
    protected boolean isWitherApplicable(AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        if (analysedComponent instanceof AnalysedOptionalComponent component && component.isIntendedConstructorParam()) {
            final String name = analysedComponent.name();
            final String withMethodName = witherOptionsPrism.withMethodPrefix() + capitalise(name) + witherOptionsPrism.withMethodSuffix();
            final MethodSpec.Builder methodBuilder = witherInterface.createMethod(withMethodName, claimableOperation, component)
                .addAnnotation(NON_NULL)
                .returns(component.parentRecord().intendedType())
                .addModifiers(Modifier.DEFAULT)
                .addJavadoc("Return a new instance with a different {@code $L} field", name)
                .addParameter(
                    ParameterSpec.builder(component.unNestedPrimaryTypeName(), name, Modifier.FINAL)
                        .addJavadoc("Replacement value")
                        .addAnnotation(NULLABLE)
                        .build()
                )
                .addStatement("return this.$L($T.ofNullable($L))", withMethodName, OPTIONAL, name);
            AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
            return true;
        }
        return false;
    }
}
