package io.github.cbarlin.aru.core.impl.types;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;

import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

public class AnalysedCollectionComponent extends AnalysedComponent {

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
        final List<? extends TypeMirror> typeArguments = decl.getTypeArguments();
        innerType = typeArguments.get(0);
        if(innerType instanceof final DeclaredType dInner) {
            final TypeElement te = (TypeElement) dInner.asElement();
            this.innerTypeName = ClassName.get(te);
        } else {
            this.innerTypeName = TypeName.get(componentType);
        }
        final TypeElement ret = (TypeElement) decl.asElement();
        final TypeMirror wrapper = utilsProcessingContext.processingEnv().getTypeUtils().erasure(ret.asType());
        final TypeElement te = (TypeElement) ((DeclaredType) wrapper).asElement();
        erasedWrapperClassName = ClassName.get(te);
    }

    private static TypeMirror list;
    private static TypeMirror set;
    private static TypeMirror queue;
    private static TypeMirror sortedSet;

    private static TypeMirror list(final ProcessingEnvironment env) {
        if(Objects.isNull(list)) {
            list = env.getTypeUtils().erasure(env.getElementUtils().getTypeElement(List.class.getCanonicalName()).asType());
        }
        return list;
    }

    private static TypeMirror set(final ProcessingEnvironment env) {
        if(Objects.isNull(set)) {
            set = env.getTypeUtils().erasure(env.getElementUtils().getTypeElement(Set.class.getCanonicalName()).asType());
        }
        return set;
    }

    private static TypeMirror sortedSet(final ProcessingEnvironment env) {
        if(Objects.isNull(set)) {
            sortedSet = env.getTypeUtils().erasure(env.getElementUtils().getTypeElement(SortedSet.class.getCanonicalName()).asType());
        }
        return sortedSet;
    }

    private static TypeMirror queue(final ProcessingEnvironment env) {
        if(Objects.isNull(queue)) {
            queue = env.getTypeUtils().erasure(env.getElementUtils().getTypeElement(Queue.class.getCanonicalName()).asType());
        }
        return queue;
    }

    public boolean isList() {
        final TypeMirror returnType = element.getAccessor().getReturnType();
        final TypeMirror collectionTypeMirror = list(processingEnv());
        final Types types = processingEnv().getTypeUtils();
        return types.isSubtype(returnType, collectionTypeMirror);
    }

    public boolean isSet() {
        final TypeMirror returnType = element.getAccessor().getReturnType();
        final TypeMirror collectionTypeMirror = set(processingEnv());
        final Types types = processingEnv().getTypeUtils();
        return types.isSubtype(returnType, collectionTypeMirror);
    }

    public boolean isSortedSet() {
        final TypeMirror returnType = element.getAccessor().getReturnType();
        final TypeMirror collectionTypeMirror = sortedSet(processingEnv());
        final Types types = processingEnv().getTypeUtils();
        return types.isSubtype(returnType, collectionTypeMirror);
    }

    public boolean isQueue() {
        final TypeMirror returnType = element.getAccessor().getReturnType();
        final TypeMirror collectionTypeMirror = queue(processingEnv());
        final Types types = processingEnv().getTypeUtils();
        return types.isSubtype(returnType, collectionTypeMirror);
    }

    @Override
    public TypeName typeName() {
        return ParameterizedTypeName.get(erasedWrapperClassName, innerTypeName);
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
}
