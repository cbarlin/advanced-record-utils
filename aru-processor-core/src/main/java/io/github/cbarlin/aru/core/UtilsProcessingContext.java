package io.github.cbarlin.aru.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;

import org.jspecify.annotations.Nullable;

import io.avaje.inject.BeanScope;
import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.analysers.TargetAnalyser;
import io.github.cbarlin.aru.core.analysers.TargetAnalysisResult;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.AnalysedType;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.visitors.InterfaceVisitor;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.core.wiring.AruGlobal;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.JavaFile;
import io.micronaut.sourcegen.javapoet.TypeSpec;

@Component
@AruGlobal
public final class UtilsProcessingContext {

    private final Map<TypeElement, ProcessingTarget> analysedTypes = new HashMap<>();
    private final Set<TypeElement> rootElements = new HashSet<>();
    private final Set<TypeElement> processedElements = new HashSet<>();

    public ProcessingEnvironment processingEnv() {
        return APContext.processingEnv();
    }

    public @Nullable ProcessingTarget analysedType(final TypeElement typeElement) {
        return analysedTypes.get(typeElement);
    }

    public Collection<ProcessingTarget> processingTargets() {
        return analysedTypes.values();
    }

    /**
     * Begin a chain of analysis from a (potential) root element
     */
    protected void analyseRootElement(final Element element, final List<TargetAnalyser> analysers) {
        // Don't re-process a generation root
        if (rootElements.contains(element) || processedElements.contains(element)) {
            return;
        }
        if (analysedTypes.containsKey(element)) {
            // Add this as a "root" but don't bother continuing
            rootElements.add(analysedTypes.get(element).typeElement());
        } else {
            analyseElement(element, Optional.empty(), analysers)
                .map(ProcessingTarget::typeElement)
                .ifPresent(rootElements::add);
        }
    }

    private Optional<ProcessingTarget> analyseElement(final Element element, final Optional<AdvRecUtilsSettings> settings, final List<TargetAnalyser> analysers) {
        if (analysedTypes.containsKey(element)) {
            return Optional.empty();
        }
        for (final TargetAnalyser analyser : analysers) {
            final TargetAnalysisResult analysisResult = analyser.analyse(element, settings);
            if (analysisResult.target().isEmpty() && analysisResult.foundElements().isEmpty()) {
                continue;
            }
            // Add to analysed types
            analysisResult.target().ifPresent(tar -> analysedTypes.put(tar.typeElement(), tar));
            final Optional<AdvRecUtilsSettings> passDown = analysisResult.target()
                .filter(AnalysedType.class::isInstance)
                .map(AnalysedType.class::cast)
                .map(AnalysedType::settings)
                .or(() -> settings);

            for (final TypeElement typeElement : analysisResult.foundElements()) {
                analyseElement(typeElement, passDown, analysers);
            }

            return analysisResult.target();
        }
        return Optional.empty();
    }

    protected void matchInterfaces() {
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

    protected void processElements(final BeanScope globalBeanScope) {
        messageCounts();
        for (final Entry<TypeElement,ProcessingTarget> entry : analysedTypes.entrySet()) {
            if (processedElements.add(entry.getKey())) {
                final ProcessingTarget target = entry.getValue();
                if (target instanceof final AnalysedRecord ar) {
                    processRecord(ar, globalBeanScope);
                } else if (target instanceof final AnalysedInterface ai) {
                    processInterface(ai, globalBeanScope);
                }
            }
        }
    }

    private void processInterface(final AnalysedInterface analysedInterface, final BeanScope globalBeanScope) {
        try (
            final BeanScope perTargetBeanScope = BeanScope.builder()
                .parent(globalBeanScope, false)
                .bean(ProcessingTarget.class, analysedInterface)
                .build();
        ) {
            perTargetBeanScope.list(InterfaceVisitor.class)
                .forEach(visitor -> visitor.visitInterface(analysedInterface));

            try {
                writeOutUtilsClass(analysedInterface);
            } catch (final Exception e) {
                APContext.messager().printError("Obtained error when writing out utils class: " + e.getMessage(), analysedInterface.typeElement());
            }
        }
    }

    private void processRecord(final AnalysedRecord analysedRecord, final BeanScope globalBeanScope) {
        try (
            final BeanScope perTargetBeanScope = BeanScope.builder()
                .parent(globalBeanScope, false)
                .bean(ProcessingTarget.class, analysedRecord)
                .profiles("aru-reset-per-record")
                .build();
        ) {
            final List<RecordVisitor> perRecordVisitors = new ArrayList<>(perTargetBeanScope.list(RecordVisitor.class));
            if (perRecordVisitors.isEmpty()) {
                APContext.messager().printError("There are no available visitors", analysedRecord.typeElement());
                return;
            }
            Collections.sort(perRecordVisitors);
            perRecordVisitors.forEach(visitor -> visitor.visitStartOfClass());
            for (final RecordComponentElement recordComponent : analysedRecord.typeElement().getRecordComponents() ) {
                processRecordElement(perTargetBeanScope, recordComponent);
            }
            perRecordVisitors.forEach(visitor -> visitor.visitEndOfClass());
            try {
                writeOutUtilsClass(analysedRecord);
            } catch (final Exception e) {
                APContext.messager().printError("Obtained error when writing out utils class: " + e.getMessage(), analysedRecord.typeElement());
            }
        }
    }

    private void processRecordElement(final BeanScope perTargetBeanScope, final RecordComponentElement recordComponent) {
        try (
            final BeanScope perElement = BeanScope.builder()
                .parent(perTargetBeanScope, false)
                .bean(RecordComponentElement.class, recordComponent)
                .build()
        ) {
            final Optional<AnalysedComponent> aco = perElement.getOptional(AnalysedComponent.class);
            if (aco.isPresent()) {
                final AnalysedComponent analysedComponent = aco.get();
                final List<RecordVisitor> perElementVisitor = new ArrayList<>(perElement.list(RecordVisitor.class));
                Collections.sort(perElementVisitor);
                perElementVisitor.forEach(visitor -> visitor.visitComponent(analysedComponent));
            }
        }
    }

    private void messageCounts() {
        int records = 0;
        int ifaces = 0;
        for (final Entry<TypeElement,ProcessingTarget> entry : analysedTypes.entrySet()) {
            if (!processedElements.contains(entry.getKey())) {
                if (entry.getValue() instanceof AnalysedRecord) {
                    records ++;
                } else if (entry.getValue() instanceof AnalysedInterface) {
                    ifaces ++;
                }
            }
        }
        APContext.messager().printNote("Advanced Record Utils: Processing round found %d records and %d interfaces to process".formatted(records, ifaces));
    }

    private void writeOutUtilsClass(final AnalysedType analysedType) throws IOException {
        analysedType.addFullGeneratedAnnotation();
        if (analysedType.utilsClass().hasContent()) {
            final Filer filer = APContext.filer();
            final TypeSpec utilsClass = analysedType.utilsClass().finishClass();
            final ClassName utilsClassName = analysedType.utilsClassName();
            final JavaFile utilsFile = JavaFile.builder(utilsClassName.packageName(), utilsClass)
                .skipJavaLangImports(true)
                .indent("    ")
                .addFileComment("Auto generated")
                .build();
            utilsFile.writeTo(filer);
            // Don't link this to the element since it looks really bad in maven output
            APContext.messager().printNote("Wrote out utils class: " + utilsClassName.canonicalName());
        }
    }

}
