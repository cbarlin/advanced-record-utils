package io.github.cbarlin.aru.core.types;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULL_MARKED;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.artifacts.GenerationArtifact;
import io.github.cbarlin.aru.prism.prison.JsonBSubTypePrism;
import io.github.cbarlin.aru.prism.prison.JsonBSubTypesPrism;
import io.github.cbarlin.aru.prism.prison.JsonSubTypesPrism;
import io.github.cbarlin.aru.prism.prison.JsonSubTypesPrism.TypePrism;
import io.github.cbarlin.aru.prism.prison.XmlSeeAlsoPrism;

public final class AnalysedInterface extends AnalysedType {
    private final Set<ProcessingTarget> implementingTypes = new TreeSet<>();

    public AnalysedInterface(final TypeElement element, final UtilsProcessingContext context, final AdvRecUtilsSettings parentSettings) {
        super(element, context, parentSettings);
        utilsClass().builder().addAnnotation(NULL_MARKED);
    }

    // Do not use `computeIfAbsent` because we may end up adding to it elsewhere
    @SuppressWarnings({"java:S3824"})
    public static AnalysedType analyse(final Map<TypeElement, ProcessingTarget> analysed, final TypeElement element, final UtilsProcessingContext context, final AdvRecUtilsSettings parentSettings) {
        context.checkForAndAddExistingUtils(element);
        if (!analysed.containsKey(element)) {
            final AnalysedInterface self = new AnalysedInterface(element, context, parentSettings);
            analysed.put(element, self);
            // Try and find all implementing classes, add them to the current class
            findImplementingClasses(analysed, self);
        }
        return (AnalysedType) analysed.get(element);
    }

    private static void findImplementingClasses(final Map<TypeElement, ProcessingTarget> analysed, final AnalysedInterface toAddInto) {
        for (final TypeMirror tm : toAddInto.typeElement.getPermittedSubclasses()) {
            extractFromTypeMirror(analysed, toAddInto, tm);
        }
        if (XmlSeeAlsoPrism.isPresent(toAddInto.typeElement)) {
            final XmlSeeAlsoPrism prism = XmlSeeAlsoPrism.getInstanceOn(toAddInto.typeElement);
            for (final TypeMirror target : prism.value()) {
                extractFromTypeMirror(analysed, toAddInto, target);
            }
        }
        if (JsonBSubTypesPrism.isPresent(toAddInto.typeElement)) {
            final JsonBSubTypesPrism prism = JsonBSubTypesPrism.getInstanceOn(toAddInto.typeElement);
            for (final JsonBSubTypePrism target : prism.value()) {
                extractFromTypeMirror(analysed, toAddInto, target.type());
            }
        }
        if (JsonSubTypesPrism.isPresent(toAddInto.typeElement)) {
            final JsonSubTypesPrism prism = JsonSubTypesPrism.getInstanceOn(toAddInto.typeElement);
            for (final TypePrism target : prism.value()) {
                extractFromTypeMirror(analysed, toAddInto, target.value());
            }
        }
    }

    private static void extractFromTypeMirror(final Map<TypeElement, ProcessingTarget> analysed, 
                                              final AnalysedInterface toAddInto,
                                              final TypeMirror targetTypeMirror
    ) {
        final TypeElement element = (TypeElement) toAddInto.processingEnv().getTypeUtils().asElement(targetTypeMirror);
        if(ElementKind.INTERFACE.equals(element.getKind())) {
            // Interfaces to interfaces :'(
            toAddInto.implementingTypes.add(AnalysedInterface.analyse(analysed, element, toAddInto.utilsProcessingContext(), toAddInto.settings()));
        } else if (ElementKind.RECORD.equals(element.getKind())) {
            toAddInto.implementingTypes.add(AnalysedRecord.analyse(analysed, element, toAddInto.utilsProcessingContext(), toAddInto.settings()));
        }
    }

    /**
     * Add a type that is known to implement this interface.
     * <p>
     * These may be discovered while processing e.g. sealed classes, `@XmlElements` annotations
     */
    public void addImplementingType(final ProcessingTarget type) {
        this.implementingTypes.add(type);
    }
    
    @Override
    public void addCrossReference(ProcessingTarget other) {
        super.addCrossReference(other);
        this.implementingTypes.forEach(pt -> {
            if (pt instanceof AnalysedType ty) {
                ty.addCrossReference(other);
            }
        });
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    public Set<ProcessingTarget> implementingTypes() {
        return Set.copyOf(implementingTypes);
    }

    public GenerationArtifact<?> builderArtifact() {
        throw new UnsupportedOperationException("Cannot provide a builder for an interface");
    }
}
