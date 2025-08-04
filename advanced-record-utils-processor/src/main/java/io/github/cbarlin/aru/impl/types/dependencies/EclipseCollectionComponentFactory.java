package io.github.cbarlin.aru.impl.types.dependencies;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.types.components.BasicAnalysedComponent;
import io.github.cbarlin.aru.impl.wiring.BasePerComponentScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__PROPERTY;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__RICH_ITERABLE;

@Factory
@BasePerComponentScope
@RequiresProperty(value = ECLIPSE_COLLECTIONS__PROPERTY, equalTo = "true")
public final class EclipseCollectionComponentFactory {

    @Bean
    Optional<EclipseAnalysedCollectionComponent> analyse(
            final RecordComponentElement element,
            final BasicAnalysedComponent basicAnalysedComponent
    ) {
        if (
                basicAnalysedComponent.componentType() instanceof final DeclaredType declaredType &&
                OptionalClassDetector.checkSameOrSubType(element, ECLIPSE_COLLECTIONS__RICH_ITERABLE) &&
                basicAnalysedComponent.typeName() instanceof ParameterizedTypeName parameterizedTypeName &&
                parameterizedTypeName.typeArguments.size() == 1
        ) {
            final List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
            if (typeArguments.size() != 1) {
                return Optional.empty();
            }
            final TypeMirror innerType = typeArguments.getFirst();
            final TypeName innerTypeName = parameterizedTypeName.typeArguments.getFirst();
            final ClassName erasedWrapperClassName = parameterizedTypeName.rawType;
            return Optional.of(
                new EclipseAnalysedCollectionComponent(basicAnalysedComponent, innerType, innerTypeName, erasedWrapperClassName)
            );
        }
        return Optional.empty();
    }
}
