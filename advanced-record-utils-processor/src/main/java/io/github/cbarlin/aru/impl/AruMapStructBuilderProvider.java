package io.github.cbarlin.aru.impl;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.AdvRecUtilsProcessor;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.AnalysedType;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.micronaut.sourcegen.javapoet.TypeName;
import org.jspecify.annotations.Nullable;
import org.mapstruct.ap.spi.BuilderInfo;
import org.mapstruct.ap.spi.BuilderProvider;
import org.mapstruct.ap.spi.TypeHierarchyErroneousException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * SPI into MapStruct that allows it to use our builders. To do this, we need to:
 * <ul>
 *     <li>Have written the builder (since MapStruct needs the finished {@link ExecutableElement} objects)</li>
 *     <li>Find the constructor for the builder and add it to MapStruct's {@link BuilderInfo.Builder#builderCreationMethod(ExecutableElement)}</li>
 *     <li>Find the "finish" method on the builder and add it to MapStruct's {@link BuilderInfo.Builder#buildMethod(Collection)}</li>
 * </ul>
 * <p>
 * We can hint to the MapStruct processor (which uses this class) that it needs to wait to the next processing round
 *   by throwing an {@link TypeHierarchyErroneousException} containing the requested {@link TypeMirror}.
 * <p>
 * We can return {@code null} if we cannot handle the type, to allow the default MapStruct behaviour.
 */
@ServiceProvider(BuilderProvider.class)
public final class AruMapStructBuilderProvider implements BuilderProvider {

    @Override
    @Nullable
    public BuilderInfo findBuilderInfo(final @Nullable TypeMirror typeMirror) {
        if (Objects.isNull(typeMirror)) {
            return null;
        }
        final UtilsProcessingContext context = obtainContext(typeMirror);
        final Optional<ProcessingTarget> target = context.analysedType(typeMirror);

        if (target.isPresent()) {
            // Excellent, we can use this!
            return createBuilderInfo(typeMirror, target.get());
        }

        if (annotationPresent(typeMirror)) {
            throw new TypeHierarchyErroneousException(typeMirror);
        }

        return null;
    }

    private static BuilderInfo createBuilderInfo(final TypeMirror typeMirror, final ProcessingTarget processingTarget) {
        final Optional<TypeElement> optUtilsClass = Optional.ofNullable(APContext.elements().getTypeElement(processingTarget.utilsClassName().canonicalName()));
        final Optional<TypeElement> optBuilderClass = Optional.ofNullable(APContext.elements().getTypeElement(processingTarget.builderArtifact().className().canonicalName()));
        if (optUtilsClass.isEmpty() || optBuilderClass.isEmpty()) {
            throw new TypeHierarchyErroneousException(typeMirror);
        }
        final TypeElement utilsClass = optUtilsClass.get();
        final TypeElement builderClass = optBuilderClass.get();
        return (new BuilderInfo.Builder())
            .buildMethod(buildMethods(builderClass, processingTarget))
            // MapStruct has handlers for null
            .builderCreationMethod(builderCreator(utilsClass, processingTarget))
            .build();
    }

    private static Collection<ExecutableElement> buildMethods(final TypeElement builderClass, final ProcessingTarget processingTarget) {
        final TypeName target = switch (processingTarget) {
            case LibraryLoadedTarget llt -> llt.intendedType();
            case AnalysedRecord ar -> ar.intendedType();
            case AnalysedType at -> at.className();
        };
        final String buildMethodName = processingTarget.prism().builderOptions().buildMethodName();
        return ElementFilter.methodsIn(builderClass.getEnclosedElements())
            .stream()
            .filter(e -> e.getParameters().isEmpty())
            .filter(e -> OptionalClassDetector.checkSameOrSubType(TypeName.get(e.getReturnType()), target))
            .filter(e -> e.getSimpleName().toString().equals(buildMethodName))
            .toList();
    }

    @Nullable
    private static ExecutableElement builderCreator(final TypeElement utilsClass, final ProcessingTarget processingTarget) {
        final String creationMethodName = processingTarget.prism().builderOptions().emptyCreationName();
        final TypeName builderClassName = processingTarget.builderArtifact().className();
        final var enclosedElements = utilsClass.getEnclosedElements();
        final var methods = ElementFilter.methodsIn(enclosedElements);
        for (final ExecutableElement method : methods) {
            if (
                method.getSimpleName().toString().equals(creationMethodName)
                    && method.getParameters().isEmpty()
                    && method.getModifiers().contains(Modifier.STATIC)
                    && method.getModifiers().contains(Modifier.PUBLIC)
                    && TypeName.get(method.getReturnType()).equals(builderClassName)
            ) {
                return method;
            }
        }
        return null;
    }

    private static UtilsProcessingContext obtainContext(final TypeMirror typeMirror) {
        final Optional<UtilsProcessingContext> optionalContext = AdvRecUtilsProcessor.globalBeanScope()
            .flatMap(beanScope -> beanScope.getOptional(UtilsProcessingContext.class))
            .filter(UtilsProcessingContext::hasPerformedAnalysis);
        if (optionalContext.isEmpty()) {
            throw new TypeHierarchyErroneousException(typeMirror);
        }
        return optionalContext.get();
    }

    private static boolean annotationPresent(final TypeMirror typeMirror) {
        return OptionalClassDetector.optionalDependencyTypeElement(typeMirror)
            .filter(AdvancedRecordUtilsPrism::isPresent)
            .isPresent();
    }

}
