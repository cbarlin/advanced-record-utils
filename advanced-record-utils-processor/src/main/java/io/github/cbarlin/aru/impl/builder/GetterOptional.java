package io.github.cbarlin.aru.impl.builder;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NOT_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.impl.types.AnalysedOptionalComponent;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public class GetterOptional extends RecordVisitor {

    public GetterOptional() {
        super(CommonsConstants.Claims.CORE_BUILDER_GETTER);
    }

    @Override
    public int specificity() {
        return 2;
    }

    @Override
    public boolean isApplicable(AnalysedRecord target) {
        return true;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        if(analysedComponent.isIntendedConstructorParam() && (analysedComponent instanceof final AnalysedOptionalComponent component)) {
            final String name = component.name();
            final MethodSpec.Builder method = component.builderArtifact()
                .createMethod(name, claimableOperation, component.element())
                .returns(component.typeName())
                .addAnnotation(NOT_NULL)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Returns the current value of {@code $L}\n", name)
                .addStatement("return $T.requireNonNullElse(this.$L, $T.empty())", OBJECTS, name, OPTIONAL);
            
            AnnotationSupplier.addGeneratedAnnotation(method, this);
            return true;
        }
        return false;
    }
    
}
