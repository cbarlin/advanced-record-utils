package io.github.cbarlin.aru.impl.diff;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.UNSUPPORTED_OPERATION_EXCEPTION;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public class DifferStaticClassGenerator extends DifferVisitor {

    public DifferStaticClassGenerator() {
        super(Claims.DIFFER_UTILS);
    }

    @Override
    protected int innerSpecificity() {
        return Integer.MAX_VALUE - 3;
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected boolean visitStartOfClassImpl(final AnalysedRecord analysedRecord) {
        AnnotationSupplier.addGeneratedAnnotation(differStaticClass, this);
        differStaticClass.builder()
            .addAnnotation(CommonsConstants.Names.NULL_MARKED)
            .addOriginatingElement(analysedRecord.typeElement())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        final MethodSpec.Builder methodBuilder = differStaticClass.createConstructor();
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        methodBuilder.modifiers.clear();
        methodBuilder.addModifiers(Modifier.PRIVATE);
        methodBuilder.addStatement("throw new $T($S)", UNSUPPORTED_OPERATION_EXCEPTION, "This is a utility class and cannot be instantiated");

        return true;
    }
}
