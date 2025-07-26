package io.github.cbarlin.aru.core.impl.types.analyser;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.COLLECTION;

import java.util.List;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.jspecify.annotations.NullUnmarked;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.impl.types.AnalysedOptionalComponent;
import io.github.cbarlin.aru.core.types.components.BasicAnalysedComponent;
import io.github.cbarlin.aru.core.wiring.ResetPerComponent;
import io.micronaut.sourcegen.javapoet.TypeName;

@Factory
@NullUnmarked
public final class OptionalAnalyser {

    @Bean
    @ResetPerComponent
    @BeanTypes(AnalysedOptionalComponent.class)
    AnalysedOptionalComponent collectionComponent(final BasicAnalysedComponent component) {
        if (OptionalClassDetector.checkSameOrSubType(component.element(), COLLECTION)) {
            final DeclaredType decl = (DeclaredType) component.componentType();
            final List<? extends TypeMirror> typeArguments = decl.getTypeArguments();
            if (typeArguments.size() != 1) {
                return null;
            }
            final TypeMirror innerType = typeArguments.get(0);
            final TypeName innerTypeName = TypeName.get(innerType);            
            return new AnalysedOptionalComponent(component, innerType, innerTypeName);
        }
        return null;
    }
}
