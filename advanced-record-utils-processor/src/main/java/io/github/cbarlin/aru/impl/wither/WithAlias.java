package io.github.cbarlin.aru.impl.wither;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.TypeAliasComponent;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public class WithAlias extends WitherVisitor {

    public WithAlias() {
        super(Claims.WITHER_WITH_ALIAS);
    }

    @Override
    protected int witherSpecificity() {
        return 3;
    }

    @Override
    protected boolean isWitherApplicable(AnalysedRecord analysedRecord) {
        // Only bother if the record has at least one TypeAlias component
        return analysedRecord.components()
            .stream()
            .anyMatch(TypeAliasComponent.class::isInstance);
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        if (analysedComponent.isIntendedConstructorParam() && analysedComponent instanceof TypeAliasComponent typeAliasComponent) {
            final String name = typeAliasComponent.name();
            final String withMethodName = witherOptionsPrism.withMethodPrefix() + capitalise(name) + witherOptionsPrism.withMethodSuffix();
            final MethodSpec.Builder methodBuilder = witherInterface.createMethod(withMethodName, claimableOperation, analysedComponent)
                .addAnnotation(NON_NULL)
                .returns(analysedComponent.parentRecord().intendedType())
                .addModifiers(Modifier.DEFAULT)
                .addJavadoc("Return a new instance with a different {@code $L} field", name)
                .addParameter(
                    ParameterSpec.builder(typeAliasComponent.serialisedTypeName(), name, Modifier.FINAL)
                        .addJavadoc("Replacement value")
                        .build()
                )
                // No need to handle null as this will eventually make its way down to the builder which will do that for us
                .addStatement("return this.$L(new $T($L))", withMethodName, typeAliasComponent.typeName(), name);
            AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
            return true;
        }
        return false;
    }
    
}
