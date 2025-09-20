package io.github.cbarlin.aru.core.mirrorhandlers;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MergedMirror implements StackingAnnotationMirror {
    private final DeclaredType annotationType;
    private final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues;
    private final List<AnnotationMirror> mergedMirrors;

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
        final ArrayList<AnnotationMirror> mirrors = new ArrayList<>(2);

        if (preferred instanceof final MergedMirror mergedMirror) {
            mirrors.addAll(mergedMirror.mergedMirrors());
        } else {
            mirrors.add(preferred);
        }

        if (secondary instanceof final MergedMirror mergedMirror) {
            mirrors.addAll(mergedMirror.mergedMirrors());
        } else {
            mirrors.add(secondary);
        }

        this.mergedMirrors = List.copyOf(mirrors);
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
    public <R, P> R accept(final AnnotationValueVisitor<R, P> v, final P p) {
        return v.visitAnnotation(this, p);
    }

    public List<AnnotationMirror> mergedMirrors() {
        return this.mergedMirrors;
    }

    @Override
    public List<SourceTrackingAnnotationMirror> trackingMirrors() {
        final ArrayList<SourceTrackingAnnotationMirror> mirrors = new ArrayList<>(mergedMirrors.size());
        for (final AnnotationMirror originalMirror : mergedMirrors) {
            if (originalMirror instanceof final StackingAnnotationMirror stam) {
                mirrors.addAll(stam.trackingMirrors());
            }
        }
        return List.copyOf(mirrors);
    }
}