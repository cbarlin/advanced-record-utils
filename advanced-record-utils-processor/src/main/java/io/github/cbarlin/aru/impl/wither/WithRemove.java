package io.github.cbarlin.aru.impl.wither;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.impl.Constants;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHelper;
import io.github.cbarlin.aru.impl.wiring.WitherPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.impl.Constants.Names.NON_NULL;

@Component
@WitherPerComponentScope
@RequiresProperty(value = "createRemoveMethods", equalTo = "true")
@RequiresBean({CollectionHandlerHelper.class, ConstructorComponent.class, AnalysedCollectionComponent.class})
public final class WithRemove extends WitherVisitor {

    public WithRemove(final WitherInterface witherInterface, final AnalysedRecord analysedRecord) {
        super(Constants.Claims.WITHER_WITH_REMOVE, witherInterface, analysedRecord);
    }

    @Override
    protected int witherSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final String name = analysedComponent.name();
        final String withMethodName = witherOptionsPrism.withMethodPrefix() + capitalise(builderOptionsPrism.removeMethodPrefix()) + capitalise(name) + builderOptionsPrism.removeMethodSuffix() + witherOptionsPrism.withMethodSuffix();
        final String builderMethodName = builderOptionsPrism.removeMethodPrefix() + capitalise(name) + builderOptionsPrism.removeMethodSuffix();
        final MethodSpec.Builder methodBuilder = witherInterface.createMethod(withMethodName, claimableOperation, analysedComponent)
                .addAnnotation(NON_NULL)
                .returns(analysedComponent.parentRecord().intendedType())
                .addModifiers(Modifier.DEFAULT)
                .addJavadoc("Return a new instance with a different {@code $L} field", name)
                .addParameter(
                        ParameterSpec.builder(analysedComponent.typeName(), name, Modifier.FINAL)
                                .addJavadoc("Items to remove from the collection")
                                .build()
                )
                .addStatement("return this.$L()\n.$L($L)\n.$L()", witherOptionsPrism.convertToBuilder(), builderMethodName, name, builderOptionsPrism.buildMethodName());
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        return true;
    }
}
