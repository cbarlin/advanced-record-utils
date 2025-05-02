package io.github.cbarlin.aru.core.mirrorhandlers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import org.apache.commons.lang3.StringUtils;

import io.github.cbarlin.aru.core.APContext;
import io.micronaut.sourcegen.javapoet.ClassName;

public class MapBasedAnnotationMirror implements AnnotationMirror {

    private final Map<ExecutableElement, AnnotationValue> elementValues;
    private final DeclaredType declaredType;

    public MapBasedAnnotationMirror(final ClassName annotationClassName) {
        this(annotationClassName, Map.of());
    }

    public MapBasedAnnotationMirror(final ClassName annotationClassName, Optional<Map<String, Object>> values) {
        this(annotationClassName, values.orElse(Map.of()));
    }

    public MapBasedAnnotationMirror(final ClassName annotationClassName, final Map<String, Object> values) {
        final Optional<TypeElement> optAte = Optional.ofNullable(APContext.typeElement(annotationClassName.canonicalName()))
            .filter((TypeElement te) -> ElementKind.ANNOTATION_TYPE.equals(te.getKind()));
        
        this.declaredType = optAte
            .map(TypeElement::asType)
            .filter(DeclaredType.class::isInstance)
            .map(DeclaredType.class::cast)
            .orElseThrow(() -> new NoSuchElementException("Cannot obtain a declared type of " + annotationClassName.canonicalName()));
        
        final TypeElement typeElement = optAte.get();
        
        final Map<ExecutableElement, AnnotationValue> out = HashMap.newHashMap(values.size());
        values.forEach((String key, Object value) -> {
            if (StringUtils.isBlank(key) || Objects.isNull(value)) {
                return;
            }
            final Optional<ExecutableElement> opt = typeElement.getEnclosedElements()
                .stream()
                .filter(ExecutableElement.class::isInstance)
                .map(ExecutableElement.class::cast)
                .filter(exec -> key.equals(exec.getSimpleName().toString()))
                .findFirst();
            if (opt.isPresent()) {
                final ExecutableElement nKey = opt.get();
                final AnnotationValue nValue = switch(value) {
                    case final Collection<?> col when (!col.isEmpty()) -> new SyntheticAnnotationValue(col.stream().filter(Objects::nonNull).map(SyntheticAnnotationValue::new).toList());
                    case final Collection<?> col when (col.isEmpty()) -> new SyntheticAnnotationValue(List.of());
                    case final Class<?> clz -> new SyntheticAnnotationValue(APContext.elements().getTypeElement(ClassName.get(clz).canonicalName()).asType());
                    case final ClassName clz -> new SyntheticAnnotationValue(APContext.elements().getTypeElement(clz.canonicalName()).asType());
                    case final Enum<?> enm -> {
                        APContext.messager().printError("Cannot process enum elements when creating synthetic annotations");
                        yield new SyntheticAnnotationValue(value);
                    }
                    case null, default -> new SyntheticAnnotationValue(value);
                };
                out.put(nKey, nValue);
            }
        });

        this.elementValues = Map.copyOf(out);
    }

    @Override
    public Map<ExecutableElement, AnnotationValue> getElementValues() {
        return elementValues;
    }

    @Override
    public DeclaredType getAnnotationType() {
        return declaredType;
    }
}
