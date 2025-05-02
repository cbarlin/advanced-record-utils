package io.github.cbarlin.aru.core.mirrorhandlers;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public record SyntheticAnnotationValue(Object value) implements AnnotationValue {

    @Override
    public Object getValue() {
        return value;
    }

    /**
     * This shouldn't be needed... Surely not...
     */
    @Override
    public final String toString() {
        return value.toString();
    }

    // Since we are constructing these... they should always be valid
    @SuppressWarnings("unchecked")
    @Override
    public <R, P> R accept(final AnnotationValueVisitor<R, P> v, final P p) {
        return switch (value) {
            case final Boolean b -> v.visitBoolean(b, p);
            case final Byte b -> v.visitByte(b, p);
            case final Character c -> v.visitChar(c, p);
            case final Double d -> v.visitDouble(d, p);
            case final Float f -> v.visitFloat(f, p);
            case final Integer i -> v.visitInt(i, p);
            case final Long l -> v.visitLong(l, p);
            case final Short s -> v.visitShort(s, p);
            case final String str -> v.visitString(str, p);
            case final TypeMirror tm -> v.visitType(tm, p);
            case final VariableElement ve when ElementKind.ENUM_CONSTANT.equals(ve.getKind()) -> v.visitEnumConstant(ve, p);
            case final AnnotationMirror am -> v.visitAnnotation(am, p);
            case final List<?> lst when (!lst.isEmpty()) && (lst.get(0) instanceof AnnotationValue) -> v.visitArray( (List<AnnotationValue>) lst, p);
            case final List<?> lst when lst.isEmpty() -> v.visitArray(List.of(), p);
            case null, default -> v.visitUnknown(this, p);
        };
    }
}