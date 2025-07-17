package io.github.cbarlin.aru.core.mirrorhandlers;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class MergedMirror implements AnnotationMirror {
    private final DeclaredType annotationType;
    private final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues;

    private static Map<ExecutableElement, AnnotationValue> basicMerge(final AnnotationMirror preferred, final AnnotationMirror secondary) {
        final Map<? extends ExecutableElement, ? extends AnnotationValue> pref = preferred.getElementValues();
        final Map<? extends ExecutableElement, ? extends AnnotationValue> sec = secondary.getElementValues();
        final Map<ExecutableElement, AnnotationValue> merged = HashMap.newHashMap(pref.size() + sec.size());

        for (final Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : pref.entrySet()) {
            merged.putIfAbsent(e.getKey(), e.getValue());
        }
        for (final Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : sec.entrySet()) {
            merged.putIfAbsent(e.getKey(), e.getValue());
        }
        return Map.copyOf(merged);
    }

    private static Map<ExecutableElement, AnnotationValue> preferTrue(final AnnotationMirror preferred, final AnnotationMirror secondary) {
        final Map<? extends ExecutableElement, ? extends AnnotationValue> pref = preferred.getElementValues();
        final Map<? extends ExecutableElement, ? extends AnnotationValue> sec = secondary.getElementValues();
        final Map<ExecutableElement, AnnotationValue> merged = HashMap.newHashMap(pref.size() + sec.size());

        for (final Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : pref.entrySet()) {
            merged.putIfAbsent(e.getKey(), e.getValue());
        }
        for (final Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : sec.entrySet()) {
            if (e.getValue().getValue() instanceof Boolean bool && Boolean.TRUE.equals(bool)) {
                merged.put(e.getKey(), e.getValue());
            } else {
                merged.putIfAbsent(e.getKey(), e.getValue());
            }
        }
        return Map.copyOf(merged);
    }

    public MergedMirror(final AnnotationMirror preferred, final AnnotationMirror secondary, final MirrorMergingBehaviour mirrorMergingBehaviour) {
        if (!preferred.getAnnotationType().toString().equals(secondary.getAnnotationType().toString())) {
            throw new IllegalArgumentException("Cannot merge two unrelated annotations together");
        }
        annotationType = preferred.getAnnotationType();
        if(mirrorMergingBehaviour.equals(MirrorMergingBehaviour.BASIC)) {
            this.elementValues = basicMerge(preferred, secondary);
        } else {
            this.elementValues = preferTrue(preferred, secondary);
        }
    }

    public MergedMirror(final AnnotationMirror preferred, final AnnotationMirror secondary) {
        this(preferred, secondary, MirrorMergingBehaviour.BASIC);
    }

    @Override
    public DeclaredType getAnnotationType() {
        return annotationType;
    }
    @Override
    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
        return elementValues;
    }
}