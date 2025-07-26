package io.github.cbarlin.aru.core;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;

import io.github.cbarlin.aru.annotations.Generated;
import io.github.cbarlin.aru.core.artifacts.IToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.OperationType;
import io.github.cbarlin.aru.core.visitors.AruVisitor;
import io.micronaut.sourcegen.javapoet.AnnotationSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@NullMarked
public final class AnnotationSupplier {
    private AnnotationSupplier() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static AnnotationSpec javaxAnnotation(final Class<?> visitor, final String comment) {
        final var builder = AnnotationSpec.builder(Generated.class)
            .addMember("value", "{$S, $S}", AdvRecUtilsProcessor.class.getCanonicalName(), visitor.getCanonicalName());

        if(StringUtils.isNotBlank(comment)) {
            builder.addMember("comments", "$S", comment);
        }

        return builder.build();
    }

    private static AnnotationSpec javaxAnnotation(final AruVisitor<?> visitor, final String comment) {
        return javaxAnnotation(visitor.getClass(), comment);
    }

    private static final String defaultComment(final ClaimableOperation claimableOperation) {
        return "Related %s claim: %s".formatted(
            claimableOperation.operationType().equals(OperationType.CLASS) ? "class" : "component",
            claimableOperation.operationName()
        );
    }

    private static String defaultComment(final AruVisitor<?> visitor) {
        return defaultComment(visitor.claimableOperation());
    }

    public static void addGeneratedAnnotation(final MethodSpec.Builder builder, final Class<?> clazz, final ClaimableOperation claimableOperation) {
        final AnnotationSpec spec = javaxAnnotation(clazz, defaultComment(claimableOperation));
        builder.addAnnotation(spec);
    }

    public static void addGeneratedAnnotation(final IToBeBuilt<?> generationArtifact, final Class<?> clazz, final ClaimableOperation claimableOperation) {
        final AnnotationSpec spec = javaxAnnotation(clazz, defaultComment(claimableOperation));
        generationArtifact.builder().addAnnotation(spec);
    }

    public static void addGeneratedAnnotation(final ToBeBuilt generationArtifact, final AruVisitor<?> visitor, final String comment) {
        final AnnotationSpec spec = javaxAnnotation(visitor, comment);
        generationArtifact.builder().addAnnotation(spec);
    }

    public static void addGeneratedAnnotation(final IToBeBuilt<?> generationArtifact, final AruVisitor<?> visitor) {
        final AnnotationSpec spec = javaxAnnotation(visitor, defaultComment(visitor));
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
