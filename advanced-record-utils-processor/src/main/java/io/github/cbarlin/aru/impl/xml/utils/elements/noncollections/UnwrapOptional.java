package io.github.cbarlin.aru.impl.xml.utils.elements.noncollections;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;
import static io.github.cbarlin.aru.impl.Constants.Names.STRING;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_WRITER;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.impl.types.AnalysedOptionalComponent;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public class UnwrapOptional extends XmlVisitor {

    public UnwrapOptional() {
        super(Claims.XML_UNWRAP_OPTIONAL);
    }

    @Override
    protected int innerSpecificity() {
        return 3;
    }

    @Override
    protected boolean innerIsApplicable(AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        if (analysedComponent instanceof AnalysedOptionalComponent component) {
            final MethodSpec.Builder methodBuilder = xmlStaticClass.createMethod(component.name(), claimableOperation, OPTIONAL);
            methodBuilder.modifiers.clear();
            methodBuilder.addParameter(
                    ParameterSpec.builder(XML_STREAM_WRITER, "output", Modifier.FINAL)
                        .addAnnotation(NON_NULL)
                        .addJavadoc("The output to write to")
                        .build()
                )
                .addParameter(
                    ParameterSpec.builder(component.typeName(), "val", Modifier.FINAL)
                        .addAnnotation(NULLABLE)
                        .addJavadoc("The item to write")
                        .build()
                )
                .addParameter(
                    ParameterSpec.builder(STRING, "currentDefaultNamespace", Modifier.FINAL)
                        .addAnnotation(NULLABLE)
                        .addJavadoc("The current default namespace")
                        .build()
                )
                .addException(XML_STREAM_EXCEPTION)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .addJavadoc("Add the {@code $L} field to the XML output", component.name())
                .addStatement("$T.$L(output, $T.nonNull(val) ? val.orElse(null) : null, currentDefaultNamespace)", xmlStaticClass.className(), component.name(), OBJECTS);
            
            AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
            return true;
        }
        return false;
    }
}
