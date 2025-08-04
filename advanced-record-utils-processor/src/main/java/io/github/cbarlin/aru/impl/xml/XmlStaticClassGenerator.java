package io.github.cbarlin.aru.impl.xml;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULL_UNMARKED;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.UNSUPPORTED_OPERATION_EXCEPTION;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.XmlPerRecordScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import jakarta.inject.Singleton;

@Singleton
@XmlPerRecordScope
public final class XmlStaticClassGenerator extends XmlVisitor {

    public XmlStaticClassGenerator(final XmlRecordHolder holder) {
        super(Claims.XML_STATIC_CLASS, holder);
    }

    @Override
    protected int innerSpecificity() {
        return 0;
    }

    @Override
    protected boolean visitStartOfClassImpl() {
        xmlStaticClass.builder()
            .addOriginatingElement(analysedRecord.typeElement())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
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
