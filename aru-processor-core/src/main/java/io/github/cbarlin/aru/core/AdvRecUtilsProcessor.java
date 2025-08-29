package io.github.cbarlin.aru.core;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import org.jspecify.annotations.Nullable;

import io.avaje.inject.BeanScope;
import io.avaje.prism.GenerateAPContext;
import io.avaje.prism.GenerateUtils;
import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtilsFull;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtilsGenerated;
import io.github.cbarlin.aru.annotations.Generated;
import io.github.cbarlin.aru.annotations.GeneratedUtil;
import io.github.cbarlin.aru.annotations.LoggingConstants;
import io.github.cbarlin.aru.annotations.TypeAlias;
import io.github.cbarlin.aru.annotations.TypeConverter;
import io.github.cbarlin.aru.core.analysers.TargetAnalyser;
import io.github.cbarlin.aru.core.factories.BeanScopeFactory;
import io.github.cbarlin.aru.core.factories.SupportedAnnotations;
import io.micronaut.sourcegen.javapoet.ClassName;

@ServiceProvider
@GenerateUtils
@GenerateAPContext
public final class AdvRecUtilsProcessor extends AbstractProcessor {

    // This is static so that certain inter-links (e.g. MapStruct), it can be accessed
    @Nullable
    private static BeanScope globalBeanScope;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public static Optional<BeanScope> globalBeanScope() {
        return Optional.ofNullable(globalBeanScope);
    }

    public static BeanScope globalBeanScope(final ProcessingEnvironment processingEnvironment) {
        if (Objects.isNull(globalBeanScope)) {
            AdvRecUtilsProcessor.globalBeanScope = BeanScopeFactory.loadGlobalScope(processingEnvironment);
        }
        return globalBeanScope;
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        final BeanScope beanScope = globalBeanScope(processingEnv);
        loadAruAnnotations();
        final SupportedAnnotations supportedAnnotations = beanScope.get(SupportedAnnotations.class);

        // First, find any meta-annotations
        findMetaAnnotations(roundEnv, supportedAnnotations.annotations());

        // OK, now loop through all those and analyse them!
        findAndProcessTargets(beanScope, roundEnv, supportedAnnotations.annotations());

        return true;
    }

    private void findAndProcessTargets(final BeanScope scope, final RoundEnvironment roundEnv, final Set<TypeElement> supportedAnnotations) {
        final UtilsProcessingContext context = scope.get(UtilsProcessingContext.class);
        final List<TargetAnalyser> analysers = scope.list(TargetAnalyser.class);
        for (final TypeElement typeElement : Set.copyOf(supportedAnnotations)) {
            for (final Element annotatedElement : roundEnv.getElementsAnnotatedWith(typeElement)) {
                context.analyseRootElement(annotatedElement, analysers);
            }
        }
        context.matchInterfaces();
        context.processElements(scope);
    }

    private void findMetaAnnotations(final RoundEnvironment roundEnv, final Set<TypeElement> supportedAnnotations) {
        for(final TypeElement annoType : Set.copyOf(supportedAnnotations)) {
            for(final Element annotatedElement : roundEnv.getElementsAnnotatedWith(annoType)) {
                if (annotatedElement instanceof TypeElement typeAnnoElement && ElementKind.ANNOTATION_TYPE.equals(typeAnnoElement.getKind())) {
                    supportedAnnotations.add(typeAnnoElement);
                }
            }
        }
    }

    @Override
    public synchronized void init(final ProcessingEnvironment env) {
        super.init(env);
        APContext.init(env);
    }   

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(
            AdvancedRecordUtils.class.getCanonicalName(),
            AdvancedRecordUtils.ImportLibraryUtils.class.getCanonicalName(),
            AdvancedRecordUtilsFull.class.getCanonicalName(),
            TypeConverter.class.getCanonicalName()
        );
    }

    private static void loadAruAnnotations() {
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtils.DEFAULT.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtils.LoggingGeneration.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtils.BuiltCollectionType.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtils.LibraryIntegration.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtils.ValidationApi.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtils.NameGeneration.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtils.DiffEvaluationMode.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtils.TypeNameOptions.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtils.BuilderOptions.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtils.WitherOptions.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtils.MergerOptions.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtils.XmlOptions.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtils.DiffOptions.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtils.TargetConstructor.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtils.ImportLibraryUtils.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtils.class));

        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtilsFull.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtilsGenerated.Version.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtilsGenerated.InternalUtil.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(AdvancedRecordUtilsGenerated.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(TypeConverter.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(Generated.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(GeneratedUtil.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(LoggingConstants.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(TypeAlias.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(Integer.class));
        OptionalClassDetector.loadAnnotation(ClassName.get(Boolean.class));
    }
}
