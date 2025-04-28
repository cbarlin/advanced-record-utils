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

import io.github.cbarlin.aru.core.artifacts.PreBuilt;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
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

    public void processElements(final Supplier<List<RecordVisitor>> visitorSupplier) {
        for (final Entry<TypeElement,ProcessingTarget> entry : analysedTypes.entrySet()) {
            if (entry.getValue() instanceof final AnalysedRecord analysedRecord && (!processedElements.contains(entry.getKey()))) {
                // Woot time to process!
                final List<RecordVisitor> visitors = visitorSupplier.get()
                    .stream()
                    .filter(vis -> vis.isApplicable(analysedRecord))
                    .toList();
                if (visitors.isEmpty()) {
                    APContext.messager().printError("There are no available visitors", analysedRecord.typeElement());
                }
                visitors.forEach(visitor -> visitor.visitStartOfClass(analysedRecord));
                analysedRecord.components().forEach(component -> visitors.forEach(visitor -> visitor.visitComponent(component)));
                visitors.forEach(visitor -> visitor.visitEndOfClass(analysedRecord));
                try {
                    writeOutUtilsClass(analysedRecord);
                } catch (final Exception e) {
                    APContext.messager().printError("Obtained error when writing out utils class: " + e.getMessage(), analysedRecord.typeElement());
                }
                processedElements.add(entry.getKey());
            }
        }
    }

    private void writeOutUtilsClass(final AnalysedRecord analysedRecord) throws IOException {
        final Filer filer = APContext.filer();
        analysedRecord.addFullGeneratedAnnotation();
        final TypeSpec utilsClass = analysedRecord.utilsClass().finishClass();
        final ClassName utilsClassName = analysedRecord.utilsClassName();
        final JavaFile utilsFile = JavaFile.builder(utilsClassName.packageName(), utilsClass)
            .skipJavaLangImports(true)
            .indent("    ")
            .addFileComment("Auto generated")
            .build();
        utilsFile.writeTo(filer);
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
            .map(t -> cnToCheckExisting(t, referencedItem))
            .flatMap(previousCompilationChecker::loadGeneratedArtifact)
            .ifPresent(utilsTypeElement -> loadGeneratedPrism(utilsTypeElement, referencedItem));
    }

    private void loadGeneratedPrism(final TypeElement utilsTypeElement, final TypeElement referencedTypeElement) {
        processedElements.add(referencedTypeElement);
        final LibraryLoadedTarget libraryLoadedTarget = new LibraryLoadedTarget(new PreBuilt(utilsTypeElement), referencedTypeElement);
        analysedTypes.put(referencedTypeElement, libraryLoadedTarget);

    }

    private ClassName cnToCheckExisting(final AdvRecUtilsSettings settings, final TypeElement typeElement) {
        final Element originaElement = settings.originalElement().orElse(typeElement);
        final String utilsClassName = settings.prism().typeNameOptions().utilsImplementationPrefix() + originaElement.getSimpleName().toString() + settings.prism().typeNameOptions().utilsImplementationSuffix();
        Element packageEl = originaElement;
        while(!(packageEl instanceof PackageElement)) {
            packageEl = packageEl.getEnclosingElement();
        }
        final String packageName = ((PackageElement) packageEl).getQualifiedName().toString();
        return ClassName.get(packageName, utilsClassName);
    }
}
