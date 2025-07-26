package io.github.cbarlin.aru.core.types;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULL_MARKED;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.lang.model.element.TypeElement;

import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.artifacts.GenerationArtifact;

public final class AnalysedInterface extends AnalysedType {
    private final SortedSet<ProcessingTarget> implementingTypes = new TreeSet<>();
    private final Set<TypeElement> impls = new HashSet<>();

    public AnalysedInterface(
        final TypeElement element, 
        final UtilsProcessingContext context, 
        final AdvRecUtilsSettings parentSettings
    ) {
        super(element, context, parentSettings);
        utilsClass().builder().addAnnotation(NULL_MARKED);
    }

    public void addUnprocessedImplementingType(final TypeElement type) {
        this.impls.add(type);
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

    public Set<TypeElement> unprocessedImplementations() {
        return Collections.unmodifiableSet(impls);
    }
    
    public Set<ProcessingTarget> implementingTypes() {
        return Collections.unmodifiableSortedSet(implementingTypes);
    }

    public GenerationArtifact<?> builderArtifact() {
        throw new UnsupportedOperationException("Cannot provide a builder for an interface");
    }
}
