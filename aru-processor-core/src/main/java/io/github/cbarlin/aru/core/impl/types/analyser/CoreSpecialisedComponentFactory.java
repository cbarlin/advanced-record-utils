package io.github.cbarlin.aru.core.impl.types.analyser;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedOptionalComponent;
import io.github.cbarlin.aru.core.types.components.BasicAnalysedComponent;
import io.github.cbarlin.aru.core.types.components.TypeConverterComponent;
import io.github.cbarlin.aru.core.wiring.CorePerComponentScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.COLLECTION;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;

@Factory
@CorePerComponentScope
public final class CoreSpecialisedComponentFactory {
    final BasicAnalysedComponent component;
    final UtilsProcessingContext utilsProcessingContext;

    public CoreSpecialisedComponentFactory(final BasicAnalysedComponent basicAnalysedComponent, final UtilsProcessingContext utilsProcessingContext) {
        this.component = basicAnalysedComponent;
        this.utilsProcessingContext = utilsProcessingContext;
    }

    @Bean
    @BeanTypes(AnalysedCollectionComponent.class)
    Optional<AnalysedCollectionComponent> collectionComponent() {
        if (OptionalClassDetector.checkSameOrSubType(component.element(), COLLECTION)) {
            final DeclaredType decl = (DeclaredType) component.componentType();
            final List<? extends TypeMirror> typeArguments = decl.getTypeArguments();
            if (typeArguments.size() != 1) {
                return Optional.empty();
            }
            final TypeMirror innerType = typeArguments.getFirst();
            final TypeName innerTypeName = TypeName.get(innerType);
            final TypeElement ret = (TypeElement) decl.asElement();
            final TypeMirror wrapper = APContext.types().erasure(ret.asType());
            final TypeElement te = (TypeElement) ((DeclaredType) wrapper).asElement();
            final ClassName erasedWrapperClassName = ClassName.get(te);

            return Optional.of(new AnalysedCollectionComponent(component, innerType, innerTypeName, erasedWrapperClassName));
        }
        return Optional.empty();
    }

    @Bean
    @BeanTypes(AnalysedOptionalComponent.class)
    Optional<AnalysedOptionalComponent> optionalComponent() {
        if (OptionalClassDetector.checkSameOrSubType(component.element(), OPTIONAL)) {
            final DeclaredType decl = (DeclaredType) component.componentType();
            final List<? extends TypeMirror> typeArguments = decl.getTypeArguments();
            if (typeArguments.size() != 1) {
                return Optional.empty();
            }
            final TypeMirror innerType = typeArguments.getFirst();
            final TypeName innerTypeName = TypeName.get(innerType);
            return Optional.of(new AnalysedOptionalComponent(component, innerType, innerTypeName));
        }
        return Optional.empty();
    }

    @Bean
    @BeanTypes(TypeConverterComponent.class)
    Optional<TypeConverterComponent> typeConverterComponent() {
        return Optional.of(component.typeName())
                .flatMap(utilsProcessingContext::obtainConverter)
                .map(lst -> new TypeConverterComponent(component, lst));
    }
}
