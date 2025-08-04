package io.github.cbarlin.aru.impl.builder;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NOT_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;

import javax.lang.model.element.Modifier;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedOptionalComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@Component
@BuilderPerComponentScope
@RequiresBean({ConstructorComponent.class, AnalysedOptionalComponent.class})
public final class GetterOptional extends RecordVisitor {

    private final AnalysedComponent component;
    private final BuilderClass builderClass;
    public GetterOptional(final AnalysedOptionalComponent aoc, final BuilderClass builderClass) {
        super(CommonsConstants.Claims.CORE_BUILDER_GETTER, aoc.parentRecord());
        this.component = aoc;
        this.builderClass = builderClass;
    }

    @Override
    public int specificity() {
        return 2;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        final String name = component.name();
        final MethodSpec.Builder method = builderClass
            .createMethod(name, claimableOperation, component.element())
            .returns(component.typeName())
            .addAnnotation(NOT_NULL)
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("Returns the current value of {@code $L}\n", name)
            .addStatement("return $T.requireNonNullElse(this.$L, $T.empty())", OBJECTS, name, OPTIONAL);
        
        AnnotationSupplier.addGeneratedAnnotation(method, this);
        return true;
    }
    
}
