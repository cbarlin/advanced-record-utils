package io.github.cbarlin.aru.impl.xml.iface;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_WRITER;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public final class ToXmlMethodNoTag extends XmlVisitor {

    public ToXmlMethodNoTag() {
        super(Claims.XML_IFACE_TO_XML_NOT_TAG);
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
    protected boolean visitStartOfClassImpl(AnalysedRecord analysedRecord) {
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
            .addStatement(
                "this.$L(output, null, null, null)", 
                xmlOptionsPrism.continueAddingToXmlMethodName()
            );
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        return true;
    }
}
