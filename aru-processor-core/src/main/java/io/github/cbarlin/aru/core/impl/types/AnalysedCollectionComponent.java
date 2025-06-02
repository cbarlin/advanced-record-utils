package io.github.cbarlin.aru.core.impl.types;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.COLLECTION;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.LIST;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.QUEUE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.SET;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.SORTED_SET;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import io.github.cbarlin.aru.core.CommonsConstants.Names;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

public class AnalysedCollectionComponent extends AnalysedComponent {

    private static final String UNWRAPPING_VARIABLE_NAME = "__innerValue";
    private final TypeMirror innerType;
    private final TypeName innerTypeName;
    private final ClassName erasedWrapperClassName;

    public AnalysedCollectionComponent(final RecordComponentElement element, 
                                       final AnalysedRecord parentRecord,
                                       final boolean isIntendedConstructorParam, 
                                       final UtilsProcessingContext utilsProcessingContext
    ) {
        super(element, parentRecord, isIntendedConstructorParam, utilsProcessingContext);
        final DeclaredType decl = (DeclaredType) componentType;
        innerType = findCollectionTypeArgument(decl);
        this.innerTypeName = TypeName.get(innerType);
        final TypeElement ret = (TypeElement) decl.asElement();
        final TypeMirror wrapper = utilsProcessingContext.processingEnv().getTypeUtils().erasure(ret.asType());
        final TypeElement te = (TypeElement) ((DeclaredType) wrapper).asElement();
        erasedWrapperClassName = ClassName.get(te);
    }

    public boolean isList() {
        return OptionalClassDetector.checkSameOrSubType(typeName, LIST);
    }

    public boolean isSet() {
        return OptionalClassDetector.checkSameOrSubType(typeName, SET);
    }

    public boolean isSortedSet() {
        return OptionalClassDetector.checkSameOrSubType(typeName, SORTED_SET);
    }

    public boolean isQueue() {
        return OptionalClassDetector.checkSameOrSubType(typeName, QUEUE);
    }

    @Override
    public Optional<ClassName> erasedWrapperTypeName() {
        return Optional.of(erasedWrapperClassName);
    }

    @Override
    public TypeMirror unNestedPrimaryComponentType() {
        return innerType;
    }

    @Override
    public TypeName unNestedPrimaryTypeName() {
        return innerTypeName;
    }

    @Override
    public boolean requiresUnwrapping() {
        return true;
    }

    @Override
    public boolean isLoopable() {
        return true;
    }

    @Override
    public TypeName serialisedTypeName() {
        // Always consider a collection of any type a simple list
        return ParameterizedTypeName.get(Names.LIST, innerTypeName);
    }

    @Override
    public void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder, final String incomingName, final TypeName unwrappedType) {
        methodBuilder.beginControlFlow("for (final $T $L : $L)", unwrappedType, UNWRAPPING_VARIABLE_NAME, incomingName);
        withUnwrappedName.accept(UNWRAPPING_VARIABLE_NAME);
        methodBuilder.endControlFlow();
    }

    private static TypeMirror findCollectionTypeArgument(TypeMirror startType) {
        final Queue<TypeMirror> tmQueue = new LinkedList<>();
        tmQueue.add(startType);

        while (!tmQueue.isEmpty()) {
            final TypeMirror currentType = tmQueue.poll();
            if (Objects.isNull(currentType) || (!TypeKind.DECLARED.equals(currentType.getKind()))) {
                continue;
            }
            final DeclaredType declaredType = (DeclaredType) currentType;
            final TypeName typeName = TypeName.get(currentType);
            if (typeName instanceof ParameterizedTypeName ptn && COLLECTION.equals(ptn.rawType)) {
                List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                if (typeArguments != null && !typeArguments.isEmpty()) {
                    return typeArguments.get(0);
                }
            } else {
                APContext.types().directSupertypes(declaredType).forEach(tmQueue::add);
            }
        }
        return null;
    }

}
