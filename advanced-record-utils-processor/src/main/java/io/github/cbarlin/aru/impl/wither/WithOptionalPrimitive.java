package io.github.cbarlin.aru.impl.wither;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.AnalysedOptionalPrimitiveComponent;
import io.github.cbarlin.aru.impl.wiring.WitherPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import jakarta.inject.Singleton;

import javax.lang.model.element.Modifier;

@Singleton
@WitherPerComponentScope
@RequiresBean({ConstructorComponent.class, AnalysedOptionalPrimitiveComponent.class})
public final class WithOptionalPrimitive extends WitherVisitor {

    private final AnalysedOptionalPrimitiveComponent component;
    public WithOptionalPrimitive(final WitherInterface witherInterface, final AnalysedRecord analysedRecord, final AnalysedOptionalPrimitiveComponent aoc) {
        super(Claims.WITHER_WITH_OPTIONAL, witherInterface, analysedRecord);
        this.component = aoc;
    }

    @Override
    protected int witherSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final String name = analysedComponent.name();
        final String withMethodName = witherOptionsPrism.withMethodPrefix() + capitalise(name) + witherOptionsPrism.withMethodSuffix();
        final MethodSpec.Builder methodBuilder = witherInterface.createMethod(withMethodName, claimableOperation, component)
            .returns(component.parentRecord().intendedType())
            .addModifiers(Modifier.DEFAULT)
            .addJavadoc("Return a new instance with a different {@code $L} field", name)
            .addParameter(
                ParameterSpec.builder(component.unNestedPrimaryTypeName(), name, Modifier.FINAL)
                    .addJavadoc("Replacement value")
                    .build()
            )
            .addStatement("return this.$L($T.of($L))", withMethodName, component.typeName(), name);
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        return true;
    }
}
