package io.github.cbarlin.aru.impl.wither;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.impl.Constants;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHelper;
import io.github.cbarlin.aru.impl.wiring.WitherPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;

@Component
@WitherPerComponentScope
@RequiresProperty(value = "createRetainAllMethod", equalTo = "true")
@RequiresBean({CollectionHandlerHelper.class, ConstructorComponent.class, AnalysedCollectionComponent.class})
public final class WithRetain extends WitherVisitor {

    private final AnalysedCollectionComponent acc;

    public WithRetain(final WitherInterface witherInterface, final AnalysedRecord analysedRecord, final AnalysedCollectionComponent acc) {
        super(Constants.Claims.WITHER_WITH_RETAIN, witherInterface, analysedRecord);
        this.acc = acc;
    }

    @Override
    protected int witherSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        if (!analysedRecord.isClaimed(Constants.Claims.BUILDER_RETAIN_ALL)) {
            return false;
        }
        final String name = analysedComponent.name();
        final String withMethodName = witherOptionsPrism.withMethodPrefix() + capitalise(builderOptionsPrism.retainMethodPrefix()) + capitalise(name) + builderOptionsPrism.retainMethodSuffix() + witherOptionsPrism.withMethodSuffix();
        final String builderMethodName = builderOptionsPrism.retainMethodPrefix() + capitalise(name) + builderOptionsPrism.retainMethodSuffix();
        final TypeName innerType = acc.unNestedPrimaryTypeName();
        final ParameterizedTypeName ptn = ParameterizedTypeName.get(CommonsConstants.Names.COLLECTION, innerType);
        final MethodSpec.Builder methodBuilder = witherInterface.createMethod(withMethodName, claimableOperation, analysedComponent)
                .returns(analysedComponent.parentRecord().intendedType())
                .addModifiers(Modifier.DEFAULT)
                .addJavadoc("Return a new instance with a different {@code $L} field", name)
                .addParameter(
                        ParameterSpec.builder(ptn, name, Modifier.FINAL)
                                .addJavadoc("Collection of items to keep in the collection")
                                .build()
                )
                .addStatement("return this.$L()\n.$L($L)\n.$L()", witherOptionsPrism.convertToBuilder(), builderMethodName, name, builderOptionsPrism.buildMethodName());
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        return true;
    }
}
