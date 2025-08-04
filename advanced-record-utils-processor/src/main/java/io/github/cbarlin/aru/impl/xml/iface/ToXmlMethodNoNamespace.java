package io.github.cbarlin.aru.impl.xml.iface;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.impl.Constants.Names.STRING;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_WRITER;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.XmlPerRecordScope;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import jakarta.inject.Singleton;

@Singleton
@XmlPerRecordScope
public final class ToXmlMethodNoNamespace extends XmlVisitor {

    public ToXmlMethodNoNamespace(final XmlRecordHolder xmlRecordHolder) {
        super(Claims.XML_IFACE_TO_XML_NO_NAMESPACE, xmlRecordHolder);
    }

    @Override
    protected int innerSpecificity() {
        return 0;
    }

    @Override
    protected boolean visitStartOfClassImpl() {
        final MethodSpec.Builder methodBuilder = xmlInterface.createMethod(xmlOptionsPrism.continueAddingToXmlMethodName(), claimableOperation)
            .addModifiers(Modifier.DEFAULT);
        methodBuilder.addJavadoc("Write out the class to the requested {@link $T}, referring to itself with the reqested tag name", XML_STREAM_WRITER)
            .addJavadoc("\n<p>\n")
            .addJavadoc("This method will close any tags it opens. It is not expected that it will start or end the document.")
            .addException(XML_STREAM_EXCEPTION)
            .addParameter(
                ParameterSpec.builder(XML_STREAM_WRITER, "output", Modifier.FINAL)
                    .addJavadoc("The output to write to")
                    .addAnnotation(NON_NULL)
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(STRING, "requestedTagName", Modifier.FINAL)
                    .addJavadoc("The tag name requested for this element. If null, will use the default tag name")
                    .addAnnotation(NULLABLE)
                    .build()
            )
            .addStatement(
                "this.$L(output, requestedTagName, null, null)", 
                xmlOptionsPrism.continueAddingToXmlMethodName()
            );
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        return true;
    }

}
