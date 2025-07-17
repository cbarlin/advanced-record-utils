package io.github.cbarlin.aru.impl.builder;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NOT_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;

import java.util.Optional;

import javax.lang.model.element.Modifier;

import org.jspecify.annotations.Nullable;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.impl.types.OptionalComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.OptionalRecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.AnalysedOptionalCollection;
import io.github.cbarlin.aru.impl.types.dependencies.EclipseAnalysedOptionalCollection;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;

@ServiceProvider
public final class ConcreteOptionalCollectionSetter extends OptionalRecordVisitor {

    public ConcreteOptionalCollectionSetter() {
        super(Claims.BUILDER_CONCRETE_OPTIONAL);
    }

    @Override
    protected int optionalSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitOptionalComponent(OptionalComponent<?> analysedComponent) {
        final Optional<ClassName> collectionTypeName = Optional.ofNullable(extractCollectionTypeName(analysedComponent));
        if (analysedComponent.isIntendedConstructorParam() && collectionTypeName.isPresent()) {
            final ParameterizedTypeName pmt = ParameterizedTypeName.get(collectionTypeName.get(), analysedComponent.unNestedPrimaryTypeName());
            final ClassName builderClassName = analysedComponent.builderArtifact().className();
            final String name = analysedComponent.name();
            final ParameterSpec param = ParameterSpec.builder(pmt, name, Modifier.FINAL)
                .addJavadoc("The replacement value")
                .addAnnotation(NULLABLE)
                .build();
            
            final MethodSpec.Builder method = analysedComponent.builderArtifact().createMethod(name, claimableOperation, analysedComponent.component())
                .addJavadoc("Updates the value of {@code $L}", name)
                .returns(builderClassName)
                .addParameter(param)
                .addAnnotation(NOT_NULL)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return this.$L($T.ofNullable($L))", name, OPTIONAL, name);
            AnnotationSupplier.addGeneratedAnnotation(method, this);
            return true;
        }
        return false;
    }

    @Nullable
    private ClassName extractCollectionTypeName(final OptionalComponent<?> component) {
        return switch (component.component()) {
            case AnalysedOptionalCollection aoc -> aoc.erasedCollectionTypeName();
            case EclipseAnalysedOptionalCollection eaoc -> eaoc.erasedCollectionTypeName();
            case null, default -> null;
        };
    }

    @Override
    public boolean isApplicable(AnalysedRecord analysedRecord) {
        return !Boolean.FALSE.equals(analysedRecord.prism().builderOptions().concreteSettersForOptional());
    }

}
