package io.github.cbarlin.aru.core.mirrorhandlers;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import java.util.HashMap;
import java.util.Map;

public final class MergedMirror implements AnnotationMirror, AnnotationValue {
    private final DeclaredType annotationType;
    private final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues;

    private static Map<ExecutableElement, AnnotationValue> basicMerge(final AnnotationMirror preferred, final AnnotationMirror secondary) {
        final Map<? extends ExecutableElement, ? extends AnnotationValue> pref = preferred.getElementValues();
        final Map<? extends ExecutableElement, ? extends AnnotationValue> sec = secondary.getElementValues();
        final Map<ExecutableElement, AnnotationValue> merged = HashMap.newHashMap(pref.size() + sec.size());

        for (final Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : pref.entrySet()) {
            if (sec.containsKey(e.getKey()) && e.getValue() instanceof final AnnotationMirror subPref && sec.get(e.getKey()) instanceof final AnnotationMirror subSec) {
                merged.putIfAbsent(e.getKey(), new MergedMirror(subPref, subSec));
            } else {
                merged.putIfAbsent(e.getKey(), e.getValue());
            }
        }
        for (final Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : sec.entrySet()) {
            merged.putIfAbsent(e.getKey(), e.getValue());
        }
        return Map.copyOf(merged);
    }

    public MergedMirror(final AnnotationMirror preferred, final AnnotationMirror secondary) {
        if (!preferred.getAnnotationType().toString().equals(secondary.getAnnotationType().toString())) {
            throw new IllegalArgumentException("Cannot merge two unrelated annotations together");
        }
        annotationType = preferred.getAnnotationType();
        this.elementValues = basicMerge(preferred, secondary);
    }

    @Override
    public DeclaredType getAnnotationType() {
        return annotationType;
    }
    @Override
    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
        return elementValues;
    }

    @Override
    public Object getValue() {
        return this;
    }

    @Override
    public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
        return v.visitAnnotation(this, p);
    }
}