package io.github.cbarlin.aru.impl.types.collection.set;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.SET;
import static io.github.cbarlin.aru.impl.Constants.Names.COLLECTIONS;
import static io.github.cbarlin.aru.impl.Constants.Names.ENUM_SET;

import javax.lang.model.element.ElementKind;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

@Component
@GlobalScope
public final class JustSetOfEnumCollectionHandler extends SetCollectionHandler {

    public JustSetOfEnumCollectionHandler() {
        super(SET, ENUM_SET);
    }

    @Override
    public boolean canHandle(final AnalysedComponent component) {
        if (component.typeName() instanceof final ParameterizedTypeName ptn && ptn.rawType.equals(SET) && !ptn.typeArguments.isEmpty()) {
            final TypeName innerTypeName = ptn.typeArguments.get(0);
            return innerTypeName instanceof final ClassName innerClassName && 
                APContext.elements().getTypeElement(innerClassName.toString()).getKind().equals(ElementKind.ENUM);
        }
        
        return false;
    }

    @Override
    public void addNonNullAutoField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName innerType) {
        EnumSetCollectionHandler.nonNullAutoField(component, addFieldTo, innerType);
    }

    @Override
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        EnumSetCollectionHandler.convertImmutable(methodBuilder, fieldName, assignmentName, innerTypeName);
    }

    @Override
    public void writeNullableAutoAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        EnumSetCollectionHandler.nullableAutoAddSingle(component, methodBuilder, innerType);
    }

    @Override
    public void writeMergerMethod(final TypeName innerType, final MethodSpec.Builder methodBuilder) {
        EnumSetCollectionHandler.mergerMethod(innerType, methodBuilder, classNameOnComponent);
    }

    @Override
    public void writeNonNullAutoGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        // Remember: these are explicitly not immutable.
        methodBuilder.returns(component.typeName())
            .addStatement("final $T<$T> ___copy = $T.copyOf(this.$L)", ENUM_SET, innerType, ENUM_SET, component.name())
            .addStatement("return ___copy");
    }

    @Override
    public void writeNonNullImmutableGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        methodBuilder.returns(component.typeName())
            .addComment("Usually you use Set.copyOf, but that internally uses a slower HashSet. This keeps the speed of an EnumSet, and maintains deep immutability by using a collection that can't be used elsewhere")
            .addStatement("final $T<$T> ___copy = $T.copyOf(this.$L)", ENUM_SET, innerType, ENUM_SET, component.name())
            .addStatement("return $T.unmodifiableSet(___copy)", COLLECTIONS);
    }
}
