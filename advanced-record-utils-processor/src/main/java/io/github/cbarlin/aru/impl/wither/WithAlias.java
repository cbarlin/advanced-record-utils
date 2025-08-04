package io.github.cbarlin.aru.impl.wither;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;

import javax.lang.model.element.Modifier;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.TypeAliasComponent;
import io.github.cbarlin.aru.impl.wiring.WitherPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import jakarta.inject.Singleton;

@Singleton
@WitherPerComponentScope
@RequiresBean({TypeAliasComponent.class, ConstructorComponent.class})
public final class WithAlias extends WitherVisitor {

    private final TypeAliasComponent typeAliasComponent;

    public WithAlias(final WitherInterface witherInterface, final AnalysedRecord analysedRecord, final TypeAliasComponent typeAliasComponent) {
        super(Claims.WITHER_WITH_ALIAS, witherInterface, analysedRecord);
        this.typeAliasComponent = typeAliasComponent;
    }

    @Override
    protected int witherSpecificity() {
        return 3;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
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
    
}
