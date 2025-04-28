package io.github.cbarlin.aru.core.impl.types;

import java.util.List;
import java.util.Optional;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;

import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.TypeName;

public class AnalysedOptionalComponent extends AnalysedComponent {

    private final TypeMirror innerType;
    private final TypeName innerTypeName;

    public AnalysedOptionalComponent(final RecordComponentElement element, 
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
    public Optional<ClassName> erasedWrapperTypeName() {
        return Optional.of(CommonsConstants.Names.OPTIONAL);
    }

    @Override
    public boolean requiresUnwrapping() {
        return true;
    }
}
