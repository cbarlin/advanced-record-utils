package io.github.cbarlin.aru.core;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;

import io.github.cbarlin.aru.annotations.Generated;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.OperationType;
import io.github.cbarlin.aru.core.visitors.AruVisitor;

import io.micronaut.sourcegen.javapoet.AnnotationSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@NullMarked
public class AnnotationSupplier {
    private AnnotationSupplier() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static AnnotationSpec javaxAnnotation(final AruVisitor<?> visitor, final String comment) {
        final var builder = AnnotationSpec.builder(Generated.class)
            .addMember("value", "{$S, $S}", AdvRecUtilsProcessor.class.getCanonicalName(), visitor.getClass().getCanonicalName());

        if(StringUtils.isNotBlank(comment)) {
            builder.addMember("comments", "$S", comment);
        }

        return builder.build();

    }

    private static String defaultComment(final AruVisitor<?> visitor) {
        return "Related %s claim: %s".formatted(
            visitor.claimableOperation().operationType().equals(OperationType.CLASS) ? "class" : "component",
            visitor.claimableOperation().operationName()
        );
    }

    public static String isoTime() {
        return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public static void addGeneratedAnnotation(final ToBeBuilt generationArtifact, final AruVisitor<?> visitor, final String comment) {
        final AnnotationSpec spec = javaxAnnotation(visitor, comment);
        generationArtifact.builder().addAnnotation(spec);
    }

    public static void addGeneratedAnnotation(final ToBeBuilt generationArtifact, final AruVisitor<?> visitor) {
        addGeneratedAnnotation(generationArtifact, visitor, defaultComment(visitor));
    }

    public static void addGeneratedAnnotation(final MethodSpec.Builder builder, final AruVisitor<?> visitor, final String comment) {
        final AnnotationSpec spec = javaxAnnotation(visitor, comment);
        builder.addAnnotation(spec);
    }

    public static void addGeneratedAnnotation(final MethodSpec.Builder builder, final AruVisitor<?> visitor) {
        addGeneratedAnnotation(builder, visitor, defaultComment(visitor));
    }
}
