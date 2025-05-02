package io.github.cbarlin.aru.impl.xml.utils;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.UNSUPPORTED_OPERATION_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.Names.STRING;
import static io.github.cbarlin.aru.impl.Constants.Names.STRINGUTILS;
import static io.github.cbarlin.aru.impl.Constants.Names.VALIDATE;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_WRITER;

import java.util.Optional;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementsPrism;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public class WriteFallback extends XmlVisitor {

    private static final String CHK_NULL_OR_TO_STRING_BLANK = "if ($T.nonNull(val) && $T.isNotBlank(val.toString()))";
    private boolean hasWarned = false;

    public WriteFallback() {
        super(Claims.XML_WRITE_FIELD);
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int innerSpecificity() {
        return 0;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        if (!hasWarned) {
            APContext.messager().printWarning("XML writer fallback has been used - one or more types are not understood. Warning will not be repeated", analysedComponent.parentRecord().typeElement());
            hasWarned = true;
        }
        
        final MethodSpec.Builder methodBuilder = xmlStaticClass.createMethod(analysedComponent.name(), claimableOperation);
        methodBuilder.modifiers.clear();
        methodBuilder.addParameter(
                ParameterSpec.builder(XML_STREAM_WRITER, "output", Modifier.FINAL)
                    .addAnnotation(NON_NULL)
                    .addJavadoc("The output to write to")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(analysedComponent.typeName(), "val", Modifier.FINAL)
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
            .addJavadoc("Add the {@code $L} field to the XML output", analysedComponent.name());
        logDebug(methodBuilder, "Component \"%s\" was not understood by the processor - falling back to writing using \"toString\"".formatted(analysedComponent.name()));
        if (XmlAttributePrism.isPresent(analysedComponent.element().getAccessor())) {
            handleAttribute(analysedComponent, methodBuilder);
        } else if (XmlElementPrism.isPresent(analysedComponent.element().getAccessor())) {
            handleElement(analysedComponent, methodBuilder);
        } else if (XmlElementsPrism.isPresent(analysedComponent.element().getAccessor())) {
            methodBuilder.addStatement("throw new $T($S)", UNSUPPORTED_OPERATION_EXCEPTION, "The fallback operator cannot handle XmlElements items");
        } else {
            APContext.messager().printError("Unable to determine output kind of record component", analysedComponent.element());
            methodBuilder.addStatement("throw new $T($S)", UNSUPPORTED_OPERATION_EXCEPTION, "No marking on record component for XML handling!");
        }

        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        return true;
    }

    private void handleElement(final AnalysedComponent analysedComponent, final MethodSpec.Builder methodBuilder) {
        final XmlElementPrism prism = XmlElementPrism.getInstanceOn(analysedComponent.element().getAccessor());
        boolean required = Boolean.TRUE.equals(prism.required());
        final String elementName = elementName(analysedComponent, prism);
        final Optional<String> namespaceName = namespaceName(prism);
        final Optional<String> defaultValue = defaultValue(prism);

        if (defaultValue.isPresent()) {
            final String writeAsDefaultValue = defaultValue.get();
            writeStartElement(methodBuilder, elementName, namespaceName);
            methodBuilder.beginControlFlow(CHK_NULL_OR_TO_STRING_BLANK, OBJECTS, STRINGUTILS);
            logTrace(methodBuilder, "Supplied value for %s (element name %s) was null/blank, writing default of %s".formatted(analysedComponent.name(), elementName, writeAsDefaultValue));
            methodBuilder.addStatement("output.writeCharacters($S)", writeAsDefaultValue)
                .nextControlFlow("else")
                .addStatement("output.writeCharacters(val.toString())")
                .endControlFlow()
                .addStatement("output.writeEndElement()");
            return;
        }

        if (required) {
            final String errMsg = XML_CANNOT_NULL_REQUIRED_ELEMENT.formatted(analysedComponent.name(), elementName);
            methodBuilder.addStatement("$T.requireNonNull(val, $S)", errMsg);
            methodBuilder.addStatement("$T.notBlank(val.toString(), $S)", VALIDATE, errMsg);
        } else {
            methodBuilder.beginControlFlow(CHK_NULL_OR_TO_STRING_BLANK, OBJECTS, STRINGUTILS);
        }

        writeStartElement(methodBuilder, elementName, namespaceName);
        methodBuilder.addStatement("output.writeCharacters(val.toString())")
            .addStatement("output.writeEndElement()");

        if (!required) {
            methodBuilder.endControlFlow();
        }
    }

    private void writeStartElement(final MethodSpec.Builder methodBuilder, final String elementName, final Optional<String> namespaceName) {
        namespaceName.ifPresentOrElse(
            namespace -> methodBuilder.addStatement("output.writeStartElement($S, $S)", namespace, elementName),
            () -> {
                logTrace(methodBuilder, "No namespace available for XML Element %s".formatted(elementName));
                methodBuilder.addStatement("output.writeStartElement($S)", elementName);
            }
        );
    }

    private void handleAttribute(final AnalysedComponent analysedComponent, final MethodSpec.Builder methodBuilder) {
        final XmlAttributePrism prism = XmlAttributePrism.getInstanceOn(analysedComponent.element().getAccessor());
        final boolean required = Boolean.TRUE.equals(prism.required());
        final String attributeName = attributeName(analysedComponent, prism);
        final Optional<String> namespaceName = namespaceName(prism);

        if (required) {
            final String errMsg = XML_CANNOT_NULL_REQUIRED_ATTRIBUTE.formatted(analysedComponent.name(), attributeName);
            methodBuilder.addStatement("$T.requireNonNull(val, $S)", errMsg);
            methodBuilder.addStatement("$T.notBlank(val.toString(), $S)", VALIDATE, errMsg);
        } else {
            methodBuilder.beginControlFlow(CHK_NULL_OR_TO_STRING_BLANK, OBJECTS, STRINGUTILS);
        }

        namespaceName.ifPresentOrElse(
            namespace -> methodBuilder.addStatement("output.writeAttribute($S, $S, val.toString())", namespace, attributeName),
            () -> methodBuilder.addStatement("output.writeAttribute($S, val.toString())", attributeName)
        );

        if (!required) {
            methodBuilder.endControlFlow();
        }
    }
}
