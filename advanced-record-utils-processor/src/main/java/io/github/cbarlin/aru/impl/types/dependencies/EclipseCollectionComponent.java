package io.github.cbarlin.aru.impl.types.dependencies;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.LIST;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__IMMUTABLE_COLLECTION;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__IMMUTABLE_LIST;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__IMMUTABLE_SET;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__MUTABLE_COLLECTION;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__MUTABLE_LIST;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__MUTABLE_SET;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

public class EclipseCollectionComponent extends AnalysedComponent {

    private static final String UNWRAPPING_VARIABLE_NAME = "__innerValue";
    private final TypeMirror innerType;
    private final TypeName innerTypeName;
    private final ClassName erasedWrapperClassName;

    public EclipseCollectionComponent(final RecordComponentElement element, 
                                      final AnalysedRecord parentRecord,
                                      final boolean isIntendedConstructorParam, 
                                      final UtilsProcessingContext utilsProcessingContext
    ) {
        super(element, parentRecord, isIntendedConstructorParam, utilsProcessingContext);
        final DeclaredType decl = (DeclaredType) componentType;
        final List<? extends TypeMirror> typeArguments = decl.getTypeArguments();
        innerType = typeArguments.get(0);
        this.innerTypeName = TypeName.get(innerType);
        final TypeElement ret = (TypeElement) decl.asElement();
        final TypeMirror wrapper = utilsProcessingContext.processingEnv().getTypeUtils().erasure(ret.asType());
        final TypeElement te = (TypeElement) ((DeclaredType) wrapper).asElement();
        erasedWrapperClassName = ClassName.get(te);
    }

    public boolean isImmutableCollection() {
        return OptionalClassDetector.checkSameOrSubType(element, ECLIPSE_COLLECTIONS__IMMUTABLE_COLLECTION);
    }

    public boolean isMutableCollection() {
        return OptionalClassDetector.checkSameOrSubType(element, ECLIPSE_COLLECTIONS__MUTABLE_COLLECTION);
    }

    public boolean isImmutableList() {
        return OptionalClassDetector.checkSameOrSubType(element, ECLIPSE_COLLECTIONS__IMMUTABLE_LIST);
    }

    public boolean isMutableList() {
        return OptionalClassDetector.checkSameOrSubType(element, ECLIPSE_COLLECTIONS__MUTABLE_LIST);
    }

    public boolean isList() {
        return isImmutableList() || isMutableList();
    }

    public boolean isImmutableSet() {
        return OptionalClassDetector.checkSameOrSubType(element, ECLIPSE_COLLECTIONS__IMMUTABLE_SET);
    }

    public boolean isMutableSet() {
        return OptionalClassDetector.checkSameOrSubType(element, ECLIPSE_COLLECTIONS__MUTABLE_SET);
    }

    public boolean isSet() {
        return isImmutableSet() || isMutableSet();
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

    @Override
    public boolean isLoopable() {
        return true;
    }

    @Override
    public TypeName serialisedTypeName() {
        // Always consider a collection of any type a simple list
        return ParameterizedTypeName.get(LIST, innerTypeName);
    }

    @Override
    public void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder, final String incomingName, final TypeName unwrappedTn) {
        methodBuilder.beginControlFlow("for (final $T $L : $L)", unwrappedTn, UNWRAPPING_VARIABLE_NAME, incomingName);
        withUnwrappedName.accept(UNWRAPPING_VARIABLE_NAME);
        methodBuilder.endControlFlow();
    }
}
