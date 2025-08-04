package io.github.cbarlin.aru.impl.types;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.types.components.BasicAnalysedComponent;
import io.github.cbarlin.aru.impl.wiring.BasePerComponentScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.COLLECTION;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL_DOUBLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL_INT;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL_LONG;
import static io.github.cbarlin.aru.impl.Constants.Names.TYPE_ALIAS;

@Factory
@BasePerComponentScope
public final class ComponentSpecialisationFactory {

    private final BasicAnalysedComponent basicAnalysedComponent;

    public ComponentSpecialisationFactory(final BasicAnalysedComponent basicAnalysedComponent){
        this.basicAnalysedComponent = basicAnalysedComponent;
    }

    @Bean
    @BeanTypes(ComponentTargetingLibraryLoaded.class)
    Optional<ComponentTargetingLibraryLoaded> targetingLibraryLoaded() {
        return Optional.of(basicAnalysedComponent)
            .flatMap(BasicAnalysedComponent::targetAnalysedType)
            .filter(LibraryLoadedTarget.class::isInstance)
            .map(LibraryLoadedTarget.class::cast)
            .map(tar -> new ComponentTargetingLibraryLoaded(basicAnalysedComponent, tar));
    }

    @Bean
    @BeanTypes(ComponentTargetingRecord.class)
    Optional<ComponentTargetingRecord> targetingRecord() {
        return Optional.of(basicAnalysedComponent)
            .flatMap(BasicAnalysedComponent::targetAnalysedType)
            .filter(AnalysedRecord.class::isInstance)
            .map(AnalysedRecord.class::cast)
            .map(tar -> new ComponentTargetingRecord(basicAnalysedComponent, tar));
    }

    @Bean
    @BeanTypes(ComponentTargetingInterface.class)
    Optional<ComponentTargetingInterface> targetingInterface() {
        return Optional.of(basicAnalysedComponent)
            .flatMap(BasicAnalysedComponent::targetAnalysedType)
            .filter(AnalysedInterface.class::isInstance)
            .map(AnalysedInterface.class::cast)
            .map(tar -> new ComponentTargetingInterface(basicAnalysedComponent, tar));
    }

    @Bean
    @BeanTypes(AnalysedOptionalPrimitiveComponent.class)
    Optional<AnalysedOptionalPrimitiveComponent> component() {
        if (OPTIONAL_LONG.equals(basicAnalysedComponent.typeName())) {
            return Optional.of(
                    new AnalysedOptionalPrimitiveComponent(basicAnalysedComponent, TypeName.LONG, APContext.types().getPrimitiveType(TypeKind.LONG))
            );
        } else if (OPTIONAL_INT.equals(basicAnalysedComponent.typeName())) {
            return Optional.of(
                    new AnalysedOptionalPrimitiveComponent(basicAnalysedComponent, TypeName.INT, APContext.types().getPrimitiveType(TypeKind.INT))
            );
        } else if (OPTIONAL_DOUBLE.equals(basicAnalysedComponent.typeName())) {
            return Optional.of(
                    new AnalysedOptionalPrimitiveComponent(basicAnalysedComponent, TypeName.DOUBLE, APContext.types().getPrimitiveType(TypeKind.DOUBLE))
            );
        }
        return Optional.empty();
    }

    @Bean
    @BeanTypes(AnalysedOptionalCollection.class)
    Optional<AnalysedOptionalCollection> analyse() {
        if (
                basicAnalysedComponent.typeName() instanceof ParameterizedTypeName optPtn &&
                optPtn.typeArguments.size() == 1 &&
                basicAnalysedComponent.componentType() instanceof final DeclaredType declaredType &&
                OptionalClassDetector.checkSameOrSubType(basicAnalysedComponent.element(), OPTIONAL) &&
                optPtn.typeArguments.getFirst() instanceof ParameterizedTypeName colPtn &&
                colPtn.typeArguments.size() == 1 &&
                OptionalClassDetector.checkSameOrSubType(colPtn.rawType, COLLECTION)
        ) {
            final List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
            if (typeArguments.size() != 1 || (!(typeArguments.getFirst() instanceof final DeclaredType collectionTypeMirror)) || collectionTypeMirror.getTypeArguments().size() != 1) {
                return Optional.empty();
            }
            final ClassName erasedCollectionTypeName = colPtn.rawType;
            final TypeName innerTypeName = colPtn.typeArguments.getFirst();
            final TypeMirror innerType = collectionTypeMirror.getTypeArguments().getFirst();
            return Optional.of(
                    new AnalysedOptionalCollection(basicAnalysedComponent, collectionTypeMirror, colPtn, erasedCollectionTypeName, innerType, innerTypeName)
            );
        }

        return Optional.empty();
    }

    @Bean
    @BeanTypes(TypeAliasComponent.class)
    Optional<TypeAliasComponent> typeAliasComponent() {
        final RecordComponentElement element = basicAnalysedComponent.element();
        if (OptionalClassDetector.checkSameOrSubType(element, TYPE_ALIAS)) {
            final Types typeUtils = APContext.types();

            final Set<TypeName> seen = new HashSet<>();
            final TypeElement te = APContext.asTypeElement(element.asType());
            final Queue<TypeMirror> interfaceQueue = new LinkedList<>(te.getInterfaces());
            final ClassName aliasName = ClassName.get(te);

            while (!interfaceQueue.isEmpty()) {
                final TypeMirror currentIfaceMirror = interfaceQueue.poll();
                if (!seen.add(TypeName.get(currentIfaceMirror))) {
                    continue; // already processed
                }
                final TypeName name = TypeName.get(currentIfaceMirror);
                if (
                        name instanceof ParameterizedTypeName ptn &&
                        TYPE_ALIAS.equals(ptn.rawType)
                ) {
                    if (ptn.typeArguments.size() == 1) {
                        return Optional.of(
                                new TypeAliasComponent(
                                        basicAnalysedComponent,
                                        ptn.typeArguments.getFirst(),
                                        aliasName
                                )
                        );
                    } else {
                        APContext.messager().printError("Found TypeAlias but it has no type arguments?", element);
                        return Optional.empty();
                    }
                }

                // Add super-interfaces of the current interface to the queue for further exploration
                if (typeUtils.asElement(currentIfaceMirror) instanceof final TypeElement ifaceElement) {
                     interfaceQueue.addAll(ifaceElement.getInterfaces());
                }
            }

            // If we reach here, TypeAlias was not found correctly in the hierarchy
            APContext.messager().printWarning(
                "Read type as a subtype of TypeAlias, but could not extract the aliased type from the hierarchy. Ignoring the aliasing for this element",
                element
            );
        }
        return Optional.empty();
    }
}
