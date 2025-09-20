package io.github.cbarlin.aru.core.mirrorhandlers;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import java.util.List;
import java.util.Map;

public final class SourceTrackingAnnotationMirror implements StackingAnnotationMirror {
    private final AnnotationMirror sourceMirror;
    private final Element annotatedElement;

    public SourceTrackingAnnotationMirror(final AnnotationMirror sourceMirror, final Element annotatedElement) {
        this.sourceMirror = sourceMirror;
        this.annotatedElement = annotatedElement;
    }

    public AnnotationMirror sourceMirror() {
        return this.sourceMirror;
    }

    public Element annotatedElement() {
        return this.annotatedElement;
    }

    @Override
    public DeclaredType getAnnotationType() {
        return sourceMirror.getAnnotationType();
    }

    @Override
    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
        return sourceMirror.getElementValues();
    }

    @Override
    public Object getValue() {
        return this;
    }

    @Override
    public <R, P> R accept(final AnnotationValueVisitor<R, P> annotationValueVisitor, final P p) {
        return annotationValueVisitor.visitAnnotation(this, p);
    }

    @Override
    public List<SourceTrackingAnnotationMirror> trackingMirrors() {
        if (sourceMirror instanceof final MergedMirror mm) {
            return mm.trackingMirrors();
        } else if (sourceMirror instanceof final SourceTrackingAnnotationMirror stam) {
            // Well, I'm clearly not the source!
            return stam.trackingMirrors();
        } else {
            return List.of(this);
        }
    }
}
