package io.github.cbarlin.aru.impl.builder;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.types.AnalysedOptionalPrimitiveComponent;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

@Component
@BuilderPerComponentScope
@RequiresBean({ConstructorComponent.class, AnalysedOptionalPrimitiveComponent.class})
public final class GetterOptionalPrimitive extends RecordVisitor {

    private final AnalysedOptionalPrimitiveComponent component;
    private final BuilderClass builderClass;
    public GetterOptionalPrimitive(final AnalysedOptionalPrimitiveComponent component, final BuilderClass builderClass) {
        super(CommonsConstants.Claims.CORE_BUILDER_GETTER, component.parentRecord());
        this.component = component;
        this.builderClass = builderClass;
    }

    @Override
    public int specificity() {
        return 2;
    }

    

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final String name = component.name();
        final MethodSpec.Builder method = builderClass
            .createMethod(name, claimableOperation, component.element())
            .returns(component.typeName())
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("Returns the current value of {@code $L}\n", name)
            .addStatement("return $T.requireNonNullElse(this.$L, $T.empty())", OBJECTS, name, component.typeName());
        
        AnnotationSupplier.addGeneratedAnnotation(method, this);
        return true;
    }
    
}
