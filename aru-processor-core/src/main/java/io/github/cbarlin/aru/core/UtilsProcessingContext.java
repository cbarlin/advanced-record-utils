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

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

@Component
@CoreGlobalScope
public final class UtilsProcessingContext {

    private final ConcurrentHashMap<TypeElement, ProcessingTarget> analysedTypes = new ConcurrentHashMap<>();
    private final ConcurrentSkipListSet<ExecutableElement> processedConverters = new ConcurrentSkipListSet<>(
            Comparator.comparing((ExecutableElement ex) -> ex.getSimpleName().toString())
                    .thenComparing(ex -> ClassName.get((TypeElement) ex.getEnclosingElement()))
    );
    private final ConcurrentHashMap<TypeName, Queue<AnalysedTypeConverter>> analysedConverters = new ConcurrentHashMap<>();
    private final ConcurrentSkipListSet<TypeElement> processedElements = new ConcurrentSkipListSet<>(Comparator.comparing(ClassName::get));
    private final ConcurrentLinkedQueue<WrittenJavaFile> pendingFiles = new ConcurrentLinkedQueue<>();
    private final ExecutorService executorService;
    private final Lock lock = new ReentrantLock();

    public UtilsProcessingContext(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public ProcessingEnvironment processingEnv() {
        return APContext.processingEnv();
    }

    public @Nullable ProcessingTarget analysedType(final TypeElement typeElement) {
        return analysedTypes.get(typeElement);
    }

    public Collection<ProcessingTarget> processingTargets() {
        return analysedTypes.values();
    }

    public Optional<List<AnalysedTypeConverter>> obtainConverter(final TypeName typeName) {
        return Optional.of(typeName)
                .filter(analysedConverters::contains)
                .map(analysedConverters::get)
                .map(List::copyOf)
                .filter(Predicate.not(List::isEmpty));
    }

    /**
     * Begin a chain of analysis from a (potential) root element
     */
    void analyseRootElement(final Element element, final List<TargetAnalyser> analysers) {
        // Don't re-process a generation root
        if (processedElements.contains(element)) {
            return;
        }
        analyseRootElement(element, Optional.empty(), analysers);
    }

    private void analyseRootElement(final Element rootElement, final Optional<AdvRecUtilsSettings> rootSettings, final List<TargetAnalyser> analysers) {
        if (analysedTypes.containsKey(rootElement) || processedConverters.contains(rootElement)) {
            return;
        }
        final LinkedHashSet<EleInQueue> queue = new LinkedHashSet<>();
        queue.addFirst(new EleInQueue(rootElement, rootSettings));
        do {
            final EleInQueue pt = queue.removeFirst();
            final Element element = pt.element();
            if (analysedTypes.containsKey(element) || processedConverters.contains(element)) {
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
        analysisResult.foundConverter().ifPresent(converter -> {
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
                    }
                }
            }
        }
    }

    void processElements(final BeanScope globalBeanScope) {
        final ProcessingEnvironment processingEnvironment = processingEnv();
        final CompletableFuture<?>[] futures = analysedTypes.entrySet().stream()
                .filter(en -> processedElements.add(en.getKey()))
                .map(Map.Entry::getValue)
                .map(target -> {
                    if (target instanceof final AnalysedRecord ar) {
                        return CompletableFuture.runAsync(() -> processRecord(ar, globalBeanScope, processingEnvironment), executorService);
                    } else if (target instanceof final AnalysedInterface ai) {
                        return CompletableFuture.runAsync(() -> processInterface(ai, globalBeanScope, processingEnvironment), executorService);
                    } else {
                        // Perform a no-op
                        return CompletableFuture.allOf();
                    }
                })
                .toArray(CompletableFuture<?>[]::new);

        CompletableFuture.allOf(futures).join();
        boolean wasErr = false;
        try {
            writeUtilsClasses();
        } catch (Exception e) {
            APContext.messager().printMessage(Diagnostic.Kind.ERROR, "Error while writing out utils class: " + e.getMessage());
            wasErr = true;
        }
        if (!wasErr) {
            APContext.messager().printMessage(Diagnostic.Kind.NOTE, "Finished processing elements");
        }
    }

    private void processInterface(final AnalysedInterface analysedInterface, final BeanScope globalBeanScope, final ProcessingEnvironment processingEnvironment) {
        APContext.init(processingEnvironment);
        try (
            final ScopeHolder scopeHolder = BeanScopeFactory.loadInterfaceScope(analysedInterface, globalBeanScope);
        ) {
            // Scope holder handles `close` on the BeanScope
            final BeanScope perTargetBeanScope = scopeHolder.scope();
            perTargetBeanScope.list(InterfaceVisitor.class)
                .forEach(InterfaceVisitor::visitInterface);

            prepareFile(analysedInterface);
        }
        APContext.clear();
    }

    private void processRecord(final AnalysedRecord analysedRecord, final BeanScope globalBeanScope, final ProcessingEnvironment processingEnvironment) {
        APContext.init(processingEnvironment);
        try (
            final ScopeHolder scopeHolder = BeanScopeFactory.loadRecordScope(analysedRecord, globalBeanScope);
        ) {
            // Scope holder handles `close` on the BeanScope
            final BeanScope perTargetBeanScope = scopeHolder.scope(); 
            final List<RecordVisitor> perRecordVisitors = new ArrayList<>(perTargetBeanScope.list(RecordVisitor.class));
            if (perRecordVisitors.isEmpty()) {
                APContext.messager().printError("There are no available visitors", analysedRecord.typeElement());
                return;
            }
            Collections.sort(perRecordVisitors);
            perRecordVisitors.forEach(RecordVisitor::visitStartOfClass);
            for (final RecordComponentElement recordComponent : analysedRecord.typeElement().getRecordComponents() ) {
                processRecordElement(perTargetBeanScope, recordComponent, analysedRecord);
            }
            perRecordVisitors.forEach(RecordVisitor::visitEndOfClass);
            prepareFile(analysedRecord);
        }
        APContext.clear();
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


    private void prepareFile(final AnalysedType analysedType) {
        analysedType.addFullGeneratedAnnotation();
        if (analysedType.utilsClass().hasContent()) {
            final TypeSpec utilsClass = analysedType.utilsClass().finishClass();
            final ClassName utilsClassName = analysedType.utilsClassName();
            final JavaFile utilsFile = JavaFile.builder(utilsClassName.packageName(), utilsClass)
                    .skipJavaLangImports(true)
                    .indent("    ")
                    .addFileComment("Auto generated")
                    .build();
            final String fileName = utilsClassName.canonicalName();
            final Element[] origination = utilsClass.originatingElements.toArray(new Element[0]);
            final StringBuilder str = new StringBuilder(129_000);
            try {
                utilsFile.writeTo(str);
            } catch (IOException e) {
                // How?!
                throw new RuntimeException(e);
            }
            final String content = str.toString();
            pendingFiles.add(new WrittenJavaFile(fileName, origination, content));
        }
    }

    private void writeUtilsClasses() throws IOException {
        final Filer filer = APContext.filer();
        for (final WrittenJavaFile pendingFile : pendingFiles) {
            final JavaFileObject destination = filer.createSourceFile(pendingFile.fileName(), pendingFile.origination());
            try (Writer writer = destination.openWriter()) {
                writer.write(pendingFile.content());
            }
            APContext.messager().printMessage(Diagnostic.Kind.NOTE, "Wrote out " + pendingFile.fileName());
        }
        pendingFiles.clear();
    }

    private record EleInQueue(
            Element element,
            Optional<AdvRecUtilsSettings> settings
    ) {}

    private record WrittenJavaFile(
            String fileName,
            Element[] origination,
            String content
    ) {}
}
