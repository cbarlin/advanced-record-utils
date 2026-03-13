package io.github.cbarlin.aru.impl.types.dependencies;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.BasicAnalysedComponent;
import io.github.cbarlin.aru.impl.types.AnalysedPrimitiveCollectionComponent;
import io.github.cbarlin.aru.impl.wiring.BasePerComponentScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__BOOLEAN_ITERABLE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__BYTE_ITERABLE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__CHAR_ITERABLE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__DOUBLE_ITERABLE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__FLOAT_ITERABLE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__INT_ITERABLE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__LONG_ITERABLE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__PRIMITIVE_ITERABLE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__PROPERTY;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__RICH_ITERABLE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__SHORT_ITERABLE;

@Factory
@BasePerComponentScope
@RequiresProperty(value = ECLIPSE_COLLECTIONS__PROPERTY, equalTo = "true")
public final class EclipseCollectionComponentFactory {

    @Bean
    @BeanTypes({EclipseAnalysedCollectionComponent.class, AnalysedCollectionComponent.class})
    Optional<EclipseAnalysedCollectionComponent> analyse(
            final RecordComponentElement element,
            final BasicAnalysedComponent basicAnalysedComponent
    ) {
        if (
                basicAnalysedComponent.componentType() instanceof final DeclaredType declaredType &&
                OptionalClassDetector.checkSameOrSubType(element, ECLIPSE_COLLECTIONS__RICH_ITERABLE) &&
                basicAnalysedComponent.typeName() instanceof final ParameterizedTypeName parameterizedTypeName &&
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

    @Bean
    @BeanTypes({EclipsePrimitiveCollectionComponent.class, AnalysedPrimitiveCollectionComponent.class, AnalysedCollectionComponent.class})
    Optional<EclipsePrimitiveCollectionComponent> analysePrimitive(
        final RecordComponentElement element,
        final BasicAnalysedComponent basicAnalysedComponent
    ) {
        if (
            basicAnalysedComponent.typeName() instanceof final ClassName cn &&
            OptionalClassDetector.checkSameOrSubType(element, ECLIPSE_COLLECTIONS__PRIMITIVE_ITERABLE)
        ) {
            // OK, now we work out which kind
            if (OptionalClassDetector.checkSameOrSubType(element, ECLIPSE_COLLECTIONS__INT_ITERABLE)) {
                return Optional.of(
                    new EclipsePrimitiveCollectionComponent(
                        basicAnalysedComponent,
                        APContext.types().getPrimitiveType(TypeKind.INT),
                        TypeName.INT,
                        cn
                    )
                );
            } else if (OptionalClassDetector.checkSameOrSubType(element, ECLIPSE_COLLECTIONS__LONG_ITERABLE)) {
                return Optional.of(
                    new EclipsePrimitiveCollectionComponent(
                        basicAnalysedComponent,
                        APContext.types().getPrimitiveType(TypeKind.LONG),
                        TypeName.LONG,
                        cn
                    )
                );
            } else if (OptionalClassDetector.checkSameOrSubType(element, ECLIPSE_COLLECTIONS__DOUBLE_ITERABLE)) {
                return Optional.of(
                    new EclipsePrimitiveCollectionComponent(
                        basicAnalysedComponent,
                        APContext.types().getPrimitiveType(TypeKind.DOUBLE),
                        TypeName.DOUBLE,
                        cn
                    )
                );
            } else if (OptionalClassDetector.checkSameOrSubType(element, ECLIPSE_COLLECTIONS__FLOAT_ITERABLE)) {
                return Optional.of(
                    new EclipsePrimitiveCollectionComponent(
                        basicAnalysedComponent,
                        APContext.types().getPrimitiveType(TypeKind.FLOAT),
                        TypeName.FLOAT,
                        cn
                    )
                );
            } else if (OptionalClassDetector.checkSameOrSubType(element, ECLIPSE_COLLECTIONS__CHAR_ITERABLE)) {
                return Optional.of(
                    new EclipsePrimitiveCollectionComponent(
                        basicAnalysedComponent,
                        APContext.types().getPrimitiveType(TypeKind.CHAR),
                        TypeName.CHAR,
                        cn
                    )
                );
            } else if (OptionalClassDetector.checkSameOrSubType(element, ECLIPSE_COLLECTIONS__SHORT_ITERABLE)) {
                return Optional.of(
                    new EclipsePrimitiveCollectionComponent(
                        basicAnalysedComponent,
                        APContext.types().getPrimitiveType(TypeKind.SHORT),
                        TypeName.SHORT,
                        cn
                    )
                );
            } else if (OptionalClassDetector.checkSameOrSubType(element, ECLIPSE_COLLECTIONS__BOOLEAN_ITERABLE)) {
                return Optional.of(
                    new EclipsePrimitiveCollectionComponent(
                        basicAnalysedComponent,
                        APContext.types().getPrimitiveType(TypeKind.BOOLEAN),
                        TypeName.BOOLEAN,
                        cn
                    )
                );
            } else if (OptionalClassDetector.checkSameOrSubType(element, ECLIPSE_COLLECTIONS__BYTE_ITERABLE)) {
                return Optional.of(
                    new EclipsePrimitiveCollectionComponent(
                        basicAnalysedComponent,
                        APContext.types().getPrimitiveType(TypeKind.BYTE),
                        TypeName.BYTE,
                        cn
                    )
                );
            }
        }
        return Optional.empty();
    }
}
