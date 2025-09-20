package io.github.cbarlin.aru.core.mirrorhandlers;

import org.jspecify.annotations.NullUnmarked;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;
import java.util.HashMap;
import java.util.Map;

public class MirrorOfDefaults
implements AnnotationMirror, AnnotationValue {

    private final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues;
    private final DeclaredType annotationType;

    public MirrorOfDefaults(final TypeElement element) {
        this.annotationType = (DeclaredType) element.asType();
        final Map<ExecutableElement, AnnotationValue> defaults = new HashMap<>(10);
        for (final ExecutableElement member : ElementFilter.methodsIn(element.getEnclosedElements())) {
            final AnnotationValue annotationValue = member.getDefaultValue();
            (new NestedVisitor(member, annotationValue)).visit(annotationValue, defaults);
        }
        this.elementValues = Map.copyOf(defaults);
    }

    public MirrorOfDefaults(final AnnotationMirror mirror) {
        annotationType = mirror.getAnnotationType();
        final Map<ExecutableElement, AnnotationValue> defaults = new HashMap<>(10);
        for (final ExecutableElement member : ElementFilter.methodsIn(mirror.getAnnotationType().asElement().getEnclosedElements())) {
            final AnnotationValue annotationValue = member.getDefaultValue();
            (new NestedVisitor(member, annotationValue)).visit(annotationValue, defaults);
        }
        this.elementValues = Map.copyOf(defaults);
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
    public <R, P> R accept(final AnnotationValueVisitor<R, P> annotationValueVisitor, final P p) {
        return annotationValueVisitor.visitAnnotation(this, p);
    }

    @NullUnmarked
    private static final class NestedVisitor extends SimpleAnnotationValueVisitor9<Object, Map<ExecutableElement,AnnotationValue>> {
        private final ExecutableElement executableElement;
        private final AnnotationValue annotationValue;

        private NestedVisitor(final ExecutableElement executableElement, final AnnotationValue annotationValue) {
            this.executableElement = executableElement;
            this.annotationValue = annotationValue;
        }

        @Override
        protected Object defaultAction(final Object o, final Map<ExecutableElement, AnnotationValue> map) {
            map.putIfAbsent(executableElement, annotationValue);
            return map;
        }

        @Override
        public Object visitAnnotation(final AnnotationMirror a, final Map<ExecutableElement, AnnotationValue> map) {
            map.putIfAbsent(executableElement, new MirrorOfDefaults(a));
            return map;
        }
    }
}
