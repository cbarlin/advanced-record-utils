package io.github.cbarlin.aru.core;

import io.avaje.inject.BeanScope;
import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.analysers.TargetAnalyser;
import io.github.cbarlin.aru.core.analysers.TargetAnalysisResult;
import io.github.cbarlin.aru.core.factories.BeanScopeFactory;
import io.github.cbarlin.aru.core.impl.ScopeHolder;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.AnalysedType;
import io.github.cbarlin.aru.core.types.AnalysedTypeConverter;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.types.components.BasicAnalysedComponent;
import io.github.cbarlin.aru.core.visitors.InterfaceVisitor;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.core.wiring.CoreGlobalScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.JavaFile;
import io.micronaut.sourcegen.javapoet.TypeName;
import io.micronaut.sourcegen.javapoet.TypeSpec;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

@Component
@CoreGlobalScope
public final class UtilsProcessingContext {

    private final Map<TypeElement, ProcessingTarget> analysedTypes = new HashMap<>();
    private final Set<ExecutableElement> processedConverters = new HashSet<>();
    private final HashMap<TypeName, Queue<AnalysedTypeConverter>> analysedConverters = new HashMap<>();
    private final Set<TypeElement> processedElements = new HashSet<>();
    private final Set<Element> rootElements = new HashSet<>();

    public ProcessingEnvironment processingEnv() {
        return APContext.processingEnv();
    }

    public boolean hasPerformedAnalysis() {
        return !this.analysedTypes.isEmpty();
    }

    public @Nullable ProcessingTarget analysedType(final TypeElement typeElement) {
        return analysedTypes.get(typeElement);
    }

    public Optional<ProcessingTarget> analysedType(final TypeMirror typeMirror) {
        return OptionalClassDetector.optionalDependencyTypeElement(typeMirror)
            .map(this::analysedType);
    }

    public Optional<ProcessingTarget> analysedType(final ClassName className) {
        return OptionalClassDetector.optionalDependencyTypeElement(className)
            .map(this::analysedType);
    }

    public Optional<Set<AnalysedTypeConverter>> obtainConverters(final TypeName typeName) {
        return Optional.of(typeName)
                .filter(analysedConverters::containsKey)
                .map(analysedConverters::get)
                .map(Set::copyOf)
                .filter(Predicate.not(Set::isEmpty));
    }

    /**
     * Begin a chain of analysis from a (potential) root element
     */
    void analyseRootElement(final Element element, final List<TargetAnalyser> analysers) {
        analyseRootElement(element, Optional.empty(), analysers);
    }

    private boolean hasBeenAnalysed(final Element element) {
        if (element instanceof final TypeElement te) {
            return analysedTypes.containsKey(te);
        } else if (element instanceof final ExecutableElement exe) {
            return processedConverters.contains(exe);
        }
        // I guess?
        return false;
    }

    private void analyseRootElement(final Element rootElement, final Optional<AdvRecUtilsSettings> rootSettings, final List<TargetAnalyser> analysers) {
        if (hasBeenAnalysed(rootElement)) {
            return;
        }
        if (rootElement instanceof TypeElement || rootElement instanceof PackageElement) {
            rootElements.add(rootElement);
        }
        final LinkedHashSet<EleInQueue> queue = new LinkedHashSet<>();
        queue.addFirst(new EleInQueue(rootElement, rootSettings));
        do {
            final EleInQueue pt = queue.removeFirst();
            final Element element = pt.element();
            if (hasBeenAnalysed(element)) {
                addAnotherRootElement(rootElement, element);
                continue;
            }
            final Optional<AdvRecUtilsSettings> settings = pt.settings();
            for (final TargetAnalyser analyser : analysers) {
                final TargetAnalysisResult analysisResult = analyser.analyse(element, settings);
                if (!(analysisResult.target().isEmpty() && analysisResult.foundElements().isEmpty() && analysisResult.foundConverter().isEmpty())) {
                    withAnalysisResult(rootSettings, analysisResult, queue, element, rootElement);
                }
            }
        } while (!queue.isEmpty());
    }

    private void addAnotherRootElement(final Element rootElement, final Element element) {
        if (element != rootElement && element instanceof final TypeElement typeElement) {
            Optional.of(analysedTypes.get(typeElement))
                .ifPresent(processingTarget -> {
                    if (processingTarget instanceof final AnalysedType analysedType) {
                        analysedType.addRootElement(rootElement, true);
                        analysedConverters.values()
                          .stream()
                          .flatMap(Queue::stream)
                          .forEach(analysedType::addTypeConverter);
                    }
                });
        }
    }

    private void withAnalysisResult(final Optional<AdvRecUtilsSettings> rootSettings, final TargetAnalysisResult analysisResult, final LinkedHashSet<EleInQueue> queue, final Element element, final Element rootElement) {
        analysisResult.target().ifPresent(tar -> analysedTypes.putIfAbsent(tar.typeElement(), tar));
        analysisResult.foundConverter()
                .forEach(converter -> {
                    final Queue<AnalysedTypeConverter> converterQueue = analysedConverters.computeIfAbsent(converter.resultingType(), ign -> new ConcurrentLinkedQueue<>());
                    converterQueue.add(converter);
                    processedConverters.add(converter.executableElement());
                });
        final Optional<AdvRecUtilsSettings> passDown = analysisResult.target()
                .filter(AnalysedType.class::isInstance)
                .map(AnalysedType.class::cast)
                .map(AnalysedType::settings)
                .or(() -> rootSettings);
        if (analysisResult.isRootElement()) {
            analysisResult.target().ifPresent(tar -> {
                rootElements.add(tar.typeElement());
                if (tar instanceof LibraryLoadedTarget && element instanceof final TypeElement tele) {
                    analysedTypes.putIfAbsent(tele, tar);
                }
            });
        } else {
            analysisResult.target().ifPresent(tar -> {
                if (tar instanceof final AnalysedType at) {
                    at.addRootElement(rootElement, true);
                }
            });
        }
        for (final TypeElement typeElement : analysisResult.foundElements()) {
            queue.add(new EleInQueue(typeElement, passDown));
        }
    }

    void matchInterfaces() {
        for (final Entry<TypeElement,ProcessingTarget> entrySet : analysedTypes.entrySet()) {
            if ((entrySet.getValue() instanceof final AnalysedInterface ai) && !processedElements.contains(entrySet.getKey())) {
                for (final TypeElement unprocessed : ai.unprocessedImplementations()) {
                    final ProcessingTarget target = analysedTypes.get(unprocessed);
                    if (Objects.nonNull(target)) {
                        ai.addImplementingType(target);
                        ai.addCrossReference(target);
                        if (target instanceof final AnalysedType at) {
                            at.addCrossReference(ai);
                        }
                    }
                }
            }
        }
    }

    void injectAllRootElements (final Set<TypeElement> metaAnnotations, final Set<String> ignoreNames) {
        // We should inject all root elements with the other known root elements
        for (final Element rootElement : rootElements) {
            if (rootElement instanceof final TypeElement te && analysedTypes.get(te) instanceof final AnalysedType at) {
                rootElements.forEach(re -> {
                    at.addRootElement(re, false);
                    metaAnnotations.forEach(meta -> {
                        if (!ignoreNames.contains(meta.getQualifiedName().toString())) {
                            at.addKnownMetaAnnotation(meta);
                        }
                    });
                });
            }
        }
    }

    void processElements(final BeanScope globalBeanScope) {
        for (final Entry<TypeElement, ProcessingTarget> entry : analysedTypes.entrySet()) {
            if (processedElements.add(entry.getKey())) {
                if (entry.getValue() instanceof final AnalysedRecord ar) {
                    processRecord(ar, globalBeanScope);
                } else if (entry.getValue() instanceof final AnalysedInterface ai) {
                    processInterface(ai, globalBeanScope);
                }
            }
        }
    }

    private void processInterface(final AnalysedInterface analysedInterface, final BeanScope globalBeanScope) {
        try (
            final ScopeHolder scopeHolder = BeanScopeFactory.loadInterfaceScope(analysedInterface, globalBeanScope)
        ) {
            // Scope holder handles `close` on the BeanScope
            final BeanScope perTargetBeanScope = scopeHolder.scope();
            perTargetBeanScope.list(InterfaceVisitor.class)
                .forEach(InterfaceVisitor::visitInterface);

            writeFile(analysedInterface);
        }
    }

    private void processRecord(final AnalysedRecord analysedRecord, final BeanScope globalBeanScope) {
        try (
            final ScopeHolder scopeHolder = BeanScopeFactory.loadRecordScope(analysedRecord, globalBeanScope)
        ) {
            // Scope holder handles `close` on the BeanScope
            final BeanScope perTargetBeanScope = scopeHolder.scope(); 
            final List<RecordVisitor> perRecordVisitors = new ArrayList<>(perTargetBeanScope.list(RecordVisitor.class));
            Collections.sort(perRecordVisitors);
            perRecordVisitors.forEach(RecordVisitor::visitStartOfClass);
            for (final RecordComponentElement recordComponent : analysedRecord.typeElement().getRecordComponents() ) {
                processRecordElement(perTargetBeanScope, recordComponent, analysedRecord);
            }
            perRecordVisitors.forEach(RecordVisitor::visitEndOfClass);
            writeFile(analysedRecord);
        }
    }

    private void processRecordElement(final BeanScope perTargetBeanScope, final RecordComponentElement recordComponent, final AnalysedRecord analysedRecord) {
        try (
            final ScopeHolder scopeHolder = BeanScopeFactory.loadComponentScope(recordComponent, perTargetBeanScope, analysedRecord)
        ) {
            // Scope holder handles `close` on the BeanScope
            final BeanScope perElement = scopeHolder.scope();
            final Optional<BasicAnalysedComponent> aco = perElement.getOptional(BasicAnalysedComponent.class);
            if (aco.isPresent()) {
                final BasicAnalysedComponent analysedComponent = aco.get();
                final List<RecordVisitor> perElementVisitor = new ArrayList<>(perElement.list(RecordVisitor.class));
                Collections.sort(perElementVisitor);
                perElementVisitor.forEach(visitor -> visitor.visitComponent(analysedComponent));
            }
        }
    }


    private void writeFile (final AnalysedType analysedType) {
        analysedType.addFullGeneratedAnnotation();
        final TypeSpec utilsClass = analysedType.utilsClass().finishClass();
        final ClassName utilsClassName = analysedType.utilsClassName();
        final JavaFile utilsFile = JavaFile.builder(utilsClassName.packageName(), utilsClass)
            .skipJavaLangImports(true)
            .indent("    ")
            .addFileComment("Auto generated")
            .build();
        try  {
            utilsFile.writeTo(APContext.filer());
            APContext.messager().printMessage(Diagnostic.Kind.NOTE, "Wrote out utils file " + utilsClassName.simpleName());
        } catch (final IOException e) {
            APContext.messager().printError("Issue writing to file", analysedType.typeElement());
        }
        analysedType.utilsClass().cleanup();
    }

    private record EleInQueue(
            Element element,
            Optional<AdvRecUtilsSettings> settings
    ) {}
}
