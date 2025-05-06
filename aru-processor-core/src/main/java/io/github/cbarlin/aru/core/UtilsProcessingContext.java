package io.github.cbarlin.aru.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import org.jspecify.annotations.Nullable;

import io.github.cbarlin.aru.core.artifacts.PreBuilt;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.AnalysedType;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.visitors.InterfaceVisitor;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.JavaFile;
import io.micronaut.sourcegen.javapoet.TypeSpec;

public class UtilsProcessingContext {
    private final ProcessingEnvironment processingEnvironment;
    private final Map<TypeElement, ProcessingTarget> analysedTypes = new HashMap<>();
    private final Set<TypeElement> rootElements = new HashSet<>();
    private final Set<TypeElement> processedElements = new HashSet<>();
    private final PreviousCompilationChecker previousCompilationChecker;

    public UtilsProcessingContext(final ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
        this.previousCompilationChecker = new PreviousCompilationChecker(processingEnvironment);
    }

    public ProcessingEnvironment processingEnv() {
        return processingEnvironment;
    }

    public @Nullable ProcessingTarget analysedType(final TypeElement typeElement) {
        return analysedTypes.get(typeElement);
    }

    public void processElements(final Supplier<List<RecordVisitor>> recordVisitorSupplier, final Supplier<List<InterfaceVisitor>> ifaceVisitorSupplier) {
        messageCounts();
        for (final Entry<TypeElement,ProcessingTarget> entry : analysedTypes.entrySet()) {
            if(!processedElements.contains(entry.getKey())) {
                if (entry.getValue() instanceof final AnalysedRecord analysedRecord) {
                    processAnalysedRecord(recordVisitorSupplier, entry.getKey(), analysedRecord);
                } else if (entry.getValue() instanceof final AnalysedInterface analysedInterface) {
                    // OK, need to load up IfaceVisitors I guess...
                    processAnalysedIface(ifaceVisitorSupplier, entry.getKey(), analysedInterface);
                }
            }
        }
    }

    private void processAnalysedIface(
        final Supplier<List<InterfaceVisitor>> visitorSupplier,
        final TypeElement element,
        final AnalysedInterface analysedInterface
    ) {
        final List<InterfaceVisitor> visitors = visitorSupplier.get()
            .stream()
            .filter(vis -> vis.isApplicable(analysedInterface))
            .toList();
        if (visitors.isEmpty()) {
            // We don't mind if there are no visitors here so no logging
            processedElements.add(element);
            return;
        }
        // The interface configures its own logger because it only has one visit method
        visitors.forEach(visitor -> visitor.visitInterface(analysedInterface));
        try {
            writeOutUtilsClass(analysedInterface);
        } catch (final Exception e) {
            APContext.messager().printError("Obtained error when writing out utils class: " + e.getMessage(), analysedInterface.typeElement());
        }
        processedElements.add(element);
    }
    

    private void processAnalysedRecord(
        final Supplier<List<RecordVisitor>> visitorSupplier,
        final TypeElement element,
        final AnalysedRecord analysedRecord
    ) {
        final List<RecordVisitor> visitors = visitorSupplier.get()
            .stream()
            .filter(vis -> vis.isApplicable(analysedRecord))
            .toList();
        if (visitors.isEmpty()) {
            APContext.messager().printError("There are no available visitors", analysedRecord.typeElement());
            processedElements.add(element);
            return;
        }
        // Configure the logging here because we may not be doing anything at a class level
        visitors.forEach(visitor -> visitor.configureLogging(analysedRecord));
        visitors.forEach(visitor -> visitor.visitStartOfClass(analysedRecord));
        analysedRecord.components().forEach(component -> visitors.forEach(visitor -> visitor.visitComponent(component)));
        visitors.forEach(visitor -> visitor.visitEndOfClass(analysedRecord));
        try {
            writeOutUtilsClass(analysedRecord);
        } catch (final Exception e) {
            APContext.messager().printError("Obtained error when writing out utils class: " + e.getMessage(), analysedRecord.typeElement());
        }
        processedElements.add(element);
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

    /**
     * Begin a chain of analysis from a (potential) root element
     */
    public void analyseRootElement(final Element element) {
        // Don't re-process a generation root
        if (rootElements.contains(element) || processedElements.contains(element)) {
            return;
        }
        if (analysedTypes.containsKey(element)) {
            // Add this as a "root" but don't bother continuing
            rootElements.add(analysedTypes.get(element).typeElement());
            return;
        }

        if(ElementKind.RECORD.equals(element.getKind()) || ElementKind.INTERFACE.equals(element.getKind())) {
            final TypeElement typeElement = (TypeElement) element;
            if (AdvRecUtilsSettings.isAnnotatated(element, processingEnvironment)) {
                rootElements.add(typeElement);
                final AdvRecUtilsSettings settings = AdvRecUtilsSettings.wrap(typeElement, processingEnvironment);
                if (ElementKind.RECORD.equals(element.getKind())) {
                    AnalysedRecord.analyse(analysedTypes, typeElement, this, settings);
                } else {
                    AnalysedInterface.analyse(analysedTypes, typeElement, this, settings);
                }
            }
        } else if (ElementKind.PACKAGE.equals(element.getKind())) {
            final PackageElement packageElement = (PackageElement) element;
            final AdvRecUtilsSettings settings = AdvRecUtilsSettings.wrap(packageElement, processingEnvironment);
            if (Boolean.TRUE.equals(settings.prism().applyToAllInPackage())) {
                packageElement.getEnclosedElements().forEach(this::analyseRootElement);
            }
        }
    }

    /**
     * Check to see if there is an existing utils class of the target item. If there is, add it to the known context (so it doesn't get re-processed)
     */
    public void checkForAndAddExistingUtils(final TypeElement referencedItem) {
        AdvRecUtilsSettings.wrapOptional(referencedItem, processingEnvironment)
            .map(this::cnToCheckExisting)
            .flatMap(previousCompilationChecker::loadGeneratedArtifact)
            .ifPresent(utilsTypeElement -> loadGeneratedPrism(utilsTypeElement, referencedItem));
    }

    private void loadGeneratedPrism(final TypeElement utilsTypeElement, final TypeElement referencedTypeElement) {
        processedElements.add(referencedTypeElement);
        final LibraryLoadedTarget libraryLoadedTarget = new LibraryLoadedTarget(new PreBuilt(utilsTypeElement), referencedTypeElement);
        analysedTypes.put(referencedTypeElement, libraryLoadedTarget);

    }

    private ClassName cnToCheckExisting(final AdvRecUtilsSettings settings) {
        final Element originaElement = settings.originalElement();
        final String utilsClassName = settings.prism().typeNameOptions().utilsImplementationPrefix() + originaElement.getSimpleName().toString() + settings.prism().typeNameOptions().utilsImplementationSuffix();
        Element packageEl = originaElement;
        while(!(packageEl instanceof PackageElement)) {
            packageEl = packageEl.getEnclosingElement();
        }
        final String packageName = ((PackageElement) packageEl).getQualifiedName().toString();
        return ClassName.get(packageName, utilsClassName);
    }
}
