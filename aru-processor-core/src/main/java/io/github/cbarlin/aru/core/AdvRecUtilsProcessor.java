package io.github.cbarlin.aru.core;

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
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.Writer;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@ServiceProvider
@GenerateUtils
@GenerateAPContext
public final class AdvRecUtilsProcessor extends AbstractProcessor {

    private static final String META_ANNOTATION_RESOURCE_PATH = "META-INF/cbarlin/metaannotations/io.github.cbarlin.aru.annotations.AdvancedRecordUtils";
    @Nullable
    private BeanScope globalBeanScope;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        Objects.requireNonNull(globalBeanScope, "Init was not called first!");
        final SupportedAnnotations supportedAnnotations = globalBeanScope.get(SupportedAnnotations.class);

        // First, find any meta-annotations
        findMetaAnnotations(roundEnv, supportedAnnotations.annotations());

        // OK, now loop through all those and analyse them!
        findAndProcessTargets(roundEnv, supportedAnnotations.annotations());

        if(roundEnv.processingOver()) {
            APContext.clear();
        }
        return true;
    }

    private void findAndProcessTargets(final RoundEnvironment roundEnv, final Set<TypeElement> supportedAnnotations) {
        Objects.requireNonNull(globalBeanScope, "Init was not called first!");
        final UtilsProcessingContext context = globalBeanScope.get(UtilsProcessingContext.class);
        final List<TargetAnalyser> analysers = globalBeanScope.list(TargetAnalyser.class);
        final ExecutorService executorService = globalBeanScope.get(ExecutorService.class);
        final CompletableFuture<?>[] futures = Set.copyOf(supportedAnnotations)
                .stream()
                .map(roundEnv::getElementsAnnotatedWith)
                .flatMap(Set::stream)
                .map(
                        (Element el) -> CompletableFuture.runAsync(
                                () -> {
                                    APContext.init(processingEnv);
                                    context.analyseRootElement(el, analysers);
                                    APContext.clear();
                                },
                                executorService
                        )
                )
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).join();
        context.matchInterfaces();
        context.processElements(globalBeanScope);
    }

    private void findMetaAnnotations(final RoundEnvironment roundEnv, final Set<TypeElement> supportedAnnotations) {
        for(final TypeElement annoType : Set.copyOf(supportedAnnotations)) {
            for(final Element annotatedElement : roundEnv.getElementsAnnotatedWith(annoType)) {
                if (annotatedElement instanceof TypeElement typeAnnoElement && ElementKind.ANNOTATION_TYPE.equals(typeAnnoElement.getKind())) {
                    // This is an annotation!
                    final String name = typeAnnoElement.getQualifiedName().toString();
                    try {
                        try(final Writer w = APContext.filer()
                            .createResource(
                                StandardLocation.CLASS_OUTPUT, 
                                "", 
                                META_ANNOTATION_RESOURCE_PATH + "/" + name, 
                                typeAnnoElement
                            )
                            .openWriter();) {
                                w.append(name);
                            }
                    } catch (Exception e) {
                        APContext.messager().printError("Error writing meta annotation: " + e.getMessage());
                    }
                    supportedAnnotations.add(typeAnnoElement);
                }
            }
        }
    }

    @Override
    public synchronized void init(final ProcessingEnvironment env) {
        super.init(env);
        APContext.init(env);
        loadAruAnnotations();
        this.globalBeanScope = BeanScopeFactory.loadGlobalScope(env);
    }   

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Objects.requireNonNull(globalBeanScope, "Init was not called first!");
        return globalBeanScope.get(SupportedAnnotations.class).annotations()
                .stream()
                .map(ClassName::get)
                .map(ClassName::canonicalName)
                .collect(Collectors.toUnmodifiableSet());
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
