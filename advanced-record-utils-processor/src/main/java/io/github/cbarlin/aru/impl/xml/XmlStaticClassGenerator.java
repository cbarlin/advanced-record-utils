package io.github.cbarlin.aru.impl.xml;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULL_UNMARKED;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.UNSUPPORTED_OPERATION_EXCEPTION;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public class XmlStaticClassGenerator extends XmlVisitor {

    public XmlStaticClassGenerator() {
        super(Claims.XML_STATIC_CLASS);
    }

    @Override
    protected int innerSpecificity() {
        return 0;
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected boolean visitStartOfClassImpl(final AnalysedRecord analysedRecord) {
        xmlStaticClass.builder()
            .addOriginatingElement(analysedRecord.typeElement())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addAnnotation(NULL_UNMARKED);
        AnnotationSupplier.addGeneratedAnnotation(xmlStaticClass, this);
        final MethodSpec.Builder methodBuilder = xmlStaticClass.createConstructor();
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        methodBuilder.modifiers.clear();
        methodBuilder.addModifiers(Modifier.PRIVATE);
        methodBuilder.addStatement("throw new $T($S)", UNSUPPORTED_OPERATION_EXCEPTION, "This is a utility class and cannot be instantiated");
        return true;
    }
}
