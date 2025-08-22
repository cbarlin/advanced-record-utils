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
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
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

    public ProcessingEnvironment processingEnv() {
        return APContext.processingEnv();
    }

    public @Nullable ProcessingTarget analysedType(final TypeElement typeElement) {
        return analysedTypes.get(typeElement);
    }

    public Optional<List<AnalysedTypeConverter>> obtainConverters(final TypeName typeName) {
        return Optional.of(typeName)
                .filter(analysedConverters::containsKey)
                .map(analysedConverters::get)
                .map(List::copyOf)
                .filter(Predicate.not(List::isEmpty));
    }

    /**
     * Begin a chain of analysis from a (potential) root element
     */
    void analyseRootElement(final Element element, final List<TargetAnalyser> analysers) {
        analyseRootElement(element, Optional.empty(), analysers);
    }

    private boolean hasBeenAnalysed(final Element element) {
        if (element instanceof TypeElement te) {
            return analysedTypes.containsKey(te);
        } else if (element instanceof ExecutableElement exe) {
            return processedConverters.contains(exe);
        }
        // I guess?
        return false;
    }

    private void analyseRootElement(final Element rootElement, final Optional<AdvRecUtilsSettings> rootSettings, final List<TargetAnalyser> analysers) {
        if (hasBeenAnalysed(rootElement)) {
            return;
        }
        final LinkedHashSet<EleInQueue> queue = new LinkedHashSet<>();
        queue.addFirst(new EleInQueue(rootElement, rootSettings));
        do {
            final EleInQueue pt = queue.removeFirst();
            final Element element = pt.element();
            if (hasBeenAnalysed(element)) {
                continue;
            }
            final Optional<AdvRecUtilsSettings> settings = pt.settings();
            for (final TargetAnalyser analyser : analysers) {
                final TargetAnalysisResult analysisResult = analyser.analyse(element, settings);
                if (!(analysisResult.target().isEmpty() && analysisResult.foundElements().isEmpty() && analysisResult.foundConverter().isEmpty())) {
                    withAnalysisResult(rootSettings, analysisResult, queue);
                }
            }
        } while (!queue.isEmpty());
    }

    private void withAnalysisResult(Optional<AdvRecUtilsSettings> rootSettings, TargetAnalysisResult analysisResult, LinkedHashSet<EleInQueue> queue) {
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
                        if (target instanceof AnalysedType at) {
                            at.addCrossReference(ai);
                        }
                    }
                }
            }
        }
    }

    void processElements(final BeanScope globalBeanScope) {
        final ProcessingEnvironment processingEnvironment = processingEnv();
        for (final Entry<TypeElement, ProcessingTarget> entry : analysedTypes.entrySet()) {
            if (processedElements.add(entry.getKey())) {
                if (entry.getValue() instanceof final AnalysedRecord ar) {
                    processRecord(ar, globalBeanScope, processingEnvironment);
                } else if (entry.getValue() instanceof final AnalysedInterface ai) {
                    processInterface(ai, globalBeanScope, processingEnvironment);
                }
            }
        }
    }

    private void processInterface(final AnalysedInterface analysedInterface, final BeanScope globalBeanScope, final ProcessingEnvironment processingEnvironment) {
        try (
            final ScopeHolder scopeHolder = BeanScopeFactory.loadInterfaceScope(analysedInterface, globalBeanScope);
        ) {
            // Scope holder handles `close` on the BeanScope
            final BeanScope perTargetBeanScope = scopeHolder.scope();
            perTargetBeanScope.list(InterfaceVisitor.class)
                .forEach(InterfaceVisitor::visitInterface);

            writeFile(analysedInterface);
        }
    }

    private void processRecord(final AnalysedRecord analysedRecord, final BeanScope globalBeanScope, final ProcessingEnvironment processingEnvironment) {
        try (
            final ScopeHolder scopeHolder = BeanScopeFactory.loadRecordScope(analysedRecord, globalBeanScope);
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
            final ScopeHolder scopeHolder = BeanScopeFactory.loadComponentScope(recordComponent, perTargetBeanScope, analysedRecord);
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
        } catch (IOException e) {
            APContext.messager().printError("Issue writing to file", analysedType.typeElement());
        }
        analysedType.utilsClass().cleanup();
    }

    private record EleInQueue(
            Element element,
            Optional<AdvRecUtilsSettings> settings
    ) {}
}
