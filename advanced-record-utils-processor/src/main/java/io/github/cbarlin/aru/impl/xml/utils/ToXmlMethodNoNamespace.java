package io.github.cbarlin.aru.impl.xml.utils;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.XmlPerRecordScope;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import jakarta.inject.Singleton;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.impl.Constants.Names.STRING;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_WRITER;

@Singleton
@XmlPerRecordScope
public final class ToXmlMethodNoNamespace extends XmlVisitor {

    public ToXmlMethodNoNamespace(final XmlRecordHolder xmlRecordHolder) {
        super(Claims.XML_STATIC_CLASS_TO_XML_NO_NAMESPACE, xmlRecordHolder);
    }

    @Override
    protected int innerSpecificity() {
        return 0;
    }

    @Override
    protected boolean visitStartOfClassImpl() {
        final var xmlStaticClassName = xmlStaticClass.className();
        final MethodSpec.Builder methodBuilder = xmlStaticClass.createMethod(STATIC_WRITE_XML_NAME, claimableOperation, xmlStaticClassName)
            .addModifiers(Modifier.STATIC);
        methodBuilder.addJavadoc("Write out the class to the requested {@link $T}, referring to itself with the reqested tag name", XML_STREAM_WRITER)
            .addJavadoc("\n<p>\n")
            .addJavadoc("This method will close any tags it opens. It is not expected that it will start or end the document.")
            .addException(XML_STREAM_EXCEPTION)
            .addParameter(
                ParameterSpec.builder(analysedRecord.intendedType(), "el", Modifier.FINAL)
                    .addJavadoc("The item to write to XML")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(XML_STREAM_WRITER, "output", Modifier.FINAL)
                    .addJavadoc("The output to write to")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(STRING.annotated(CommonsConstants.NULLABLE_ANNOTATION), "requestedTagName", Modifier.FINAL)
                    .addJavadoc("The tag name requested for this element. If null, will use the default tag name")
                    .build()
            )
            .addStatement(
                "$T.$L(el, output, requestedTagName, null, null)", 
                xmlStaticClassName, 
                STATIC_WRITE_XML_NAME
            );
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        return true;
    }
}
