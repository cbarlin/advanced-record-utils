package io.github.cbarlin.aru.core.mirrorhandlers;

import io.github.cbarlin.aru.annotations.Generated;
import io.github.cbarlin.aru.core.APContext;
import io.micronaut.sourcegen.javapoet.AnnotationSpec;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.Util;
import org.jspecify.annotations.NullUnmarked;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Exists because the default AnnotationSpec builder isn't pretty when it comes to outputting
 */
public final class ToPrettierAnnotationSpec {

    @Generated("STATIC_CLASS_CONSTRUCTOR")
    private ToPrettierAnnotationSpec() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static AnnotationSpec convertToAnnotationSpec(final AnnotationMirror annotationMirror) {
        final TypeElement typeElement = APContext.asTypeElement(annotationMirror.getAnnotationType());
        final ClassName name = ClassName.get(typeElement);
        final AnnotationSpec.Builder builder = AnnotationSpec.builder(name);
        final NestedVisitor nestedVisitor = new NestedVisitor(builder);
        for (final Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            final AnnotationValue av = entry.getValue();
            final String valueName = entry.getKey().getSimpleName().toString();
            av.accept(nestedVisitor, valueName);
        }
        return builder.build();
    }

    @NullUnmarked
    private static final class NestedVisitor extends SimpleAnnotationValueVisitor9<AnnotationSpec.Builder, String> {
        private final AnnotationSpec.Builder builder;

        public NestedVisitor(final AnnotationSpec.Builder builder) {
            this.builder = builder;
        }

        @Override
        protected AnnotationSpec.Builder defaultAction(final Object value, final String s) {
            if (Objects.isNull(value) || Objects.isNull(s) || s.isBlank()) {
                return builder;
            }
            switch (value) {
                case final Class<?> clz -> builder.addMember(s, "$T.class", clz);
                case final ClassName clz -> builder.addMember(s, "$T.class", clz);
                case final Enum<?> enm -> builder.addMember(s, "$T.$L", value.getClass(), enm.name());
                case final String str -> builder.addMember(s, "$S", str);
                case final Float fl -> builder.addMember(s, "$Lf", fl);
                case final Character chr -> builder.addMember(s, "'$L'", Util.characterLiteralWithoutSingleQuotes(chr));
                default -> builder.addMember(s,"$L", value);
            }
            return builder;
        }

        @Override
        public AnnotationSpec.Builder visitAnnotation(final AnnotationMirror a, final String s) {
            return builder.addMember(s, convertToAnnotationSpec(a));
        }

        @Override
        public AnnotationSpec.Builder visitEnumConstant(final VariableElement c, final String name) {
            return builder.addMember(name, "$T.$L", c.asType(), c.getSimpleName());
        }

        @Override
        public AnnotationSpec.Builder visitType(final TypeMirror t, final String name) {
            return builder.addMember(name, "$T.class", t);
        }

        @Override
        public AnnotationSpec.Builder visitArray(final List<? extends AnnotationValue> values, final String name) {
            for (final AnnotationValue value : values) {
                value.accept(this, name);
            }
            return builder;
        }
    }

}
