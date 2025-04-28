package io.github.cbarlin.aru.impl.merger;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.UNSUPPORTED_OPERATION_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.MERGER_UTILS_CLASS;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public class MergerStaticGenerator extends RecordVisitor {

    public MergerStaticGenerator() {
        super(Claims.MERGER_STATIC_CLASS);
    }

    @Override
    public int specificity() {
        return Integer.MAX_VALUE - 2;
    }

    @Override
    public boolean isApplicable(final AnalysedRecord analysedRecord) {
        return Boolean.TRUE.equals(analysedRecord.settings().prism().merger());
    }

    @Override
    protected boolean visitStartOfClassImpl(final AnalysedRecord analysedRecord) {
        final var builder = analysedRecord.utilsClassChildClass(MERGER_UTILS_CLASS, claimableOperation);
        AnnotationSupplier.addGeneratedAnnotation(builder, this);
        builder.builder()
            .addAnnotation(CommonsConstants.Names.NULL_MARKED)
            .addOriginatingElement(analysedRecord.typeElement())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        
        final MethodSpec.Builder methodBuilder = builder.createConstructor();
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        methodBuilder.modifiers.clear();
        methodBuilder.addModifiers(Modifier.PRIVATE);
        methodBuilder.addStatement("throw new $T($S)", UNSUPPORTED_OPERATION_EXCEPTION, "This is a utility class and cannot be instantiated");
        return true;
    }
}
