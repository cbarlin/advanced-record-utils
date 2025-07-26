package io.github.cbarlin.aru.impl.xml.utils;

import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedOptionalComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.XmlPerComponentScope;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlTransientPrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;
import jakarta.inject.Singleton;

import javax.lang.model.element.Modifier;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.UNSUPPORTED_OPERATION_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.Names.ILLEGAL_ARGUMENT_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.Names.STRING;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_WRITER;

@Singleton
@XmlPerComponentScope
public final class WriteFallback extends XmlVisitor {

    private static final String CHK_NOT_NULL_OR_BLANK = "if ($T.nonNull(val) && $T.nonNull(val.toString()) && (!val.toString().isBlank()) )";
    private static final AtomicBoolean HAS_WARNED = new AtomicBoolean(false);

    private final Optional<XmlAttributePrism> xmlAttributePrism;
    private final Optional<XmlElementPrism> xmlElementPrism;
    private final Optional<XmlTransientPrism> xmlTransientPrism;
    private final Optional<AnalysedOptionalComponent> analysedOptionalComponent;

    public WriteFallback(
        final XmlRecordHolder xmlRecordHolder,
        final Optional<XmlAttributePrism> xmlAttributePrism,
        final Optional<XmlElementPrism> xmlElementPrism,
        final Optional<XmlTransientPrism> xmlTransientPrism,
        final Optional<AnalysedOptionalComponent> analysedOptionalComponent
    ) {
        super(Claims.XML_WRITE_FIELD, xmlRecordHolder);
        this.xmlAttributePrism = xmlAttributePrism;
        this.xmlElementPrism = xmlElementPrism;
        this.xmlTransientPrism = xmlTransientPrism;
        this.analysedOptionalComponent = analysedOptionalComponent;
    }

    @Override
    protected int innerSpecificity() {
        return Integer.MIN_VALUE;
    }

    // We only want to warn once per compilation so change the static field
    @SuppressWarnings({"java:S2696"})
    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        if (HAS_WARNED.compareAndSet(false, true)) {
            APContext.messager().printWarning("XML writer fallback has been used - one or more types are not understood. Warning will not be repeated");
        }
        final TypeName accepterTypeName = analysedOptionalComponent.map(AnalysedOptionalComponent::serialisedTypeName)
                .orElse(analysedComponent.serialisedTypeName());
        final MethodSpec.Builder methodBuilder = xmlStaticClass.createMethod(analysedComponent.name(), claimableOperation);
        methodBuilder.modifiers.clear();
        methodBuilder.addParameter(
                ParameterSpec.builder(XML_STREAM_WRITER, "output", Modifier.FINAL)
                    .addAnnotation(NON_NULL)
                    .addJavadoc("The output to write to")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(accepterTypeName, "val", Modifier.FINAL)
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
        if (xmlAttributePrism.isPresent()) {
            handleAttribute(analysedComponent, methodBuilder);
        } else if (xmlElementPrism.isPresent()) {
            handleElement(analysedComponent, methodBuilder);
        } else if (xmlTransientPrism.isPresent()) {
            methodBuilder.addComment("$L", "No-op call to transient element");
        } else {
            APContext.messager().printError("Unable to determine output kind of record component", analysedComponent.element());
            methodBuilder.addStatement("throw new $T($S)", UNSUPPORTED_OPERATION_EXCEPTION, "No marking on record component for XML handling!");
        }

        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        return true;
    }

    private void handleElement(final AnalysedComponent analysedComponent, final MethodSpec.Builder methodBuilder) {
        final XmlElementPrism prism = xmlElementPrism.orElseThrow();
        final boolean required = Boolean.TRUE.equals(prism.required());
        final String elementName = findElementName(analysedComponent, prism);
        final Optional<String> namespaceName = namespaceName(prism);
        final Optional<String> defaultValue = defaultValue(prism);

        if (defaultValue.isPresent()) {
            final String writeAsDefaultValue = defaultValue.get();
            writeStartElement(methodBuilder, elementName, namespaceName);
            methodBuilder.beginControlFlow(CHK_NOT_NULL_OR_BLANK, OBJECTS, OBJECTS)
                .addStatement("output.writeCharacters(val.toString())")
                .nextControlFlow("else");
            logTrace(methodBuilder, "Supplied value for %s (element name %s) was null/blank, writing default of %s".formatted(analysedComponent.name(), elementName, writeAsDefaultValue));
            methodBuilder.addStatement("output.writeCharacters($S)", writeAsDefaultValue)
                .endControlFlow()
                .addStatement("output.writeEndElement()");
            return;
        }

        if (required) {
            final String errMsg = XML_CANNOT_NULL_REQUIRED_ELEMENT.formatted(analysedComponent.name(), elementName);
            methodBuilder.addStatement("$T.requireNonNull(val, $S)", OBJECTS, errMsg);
            methodBuilder.addStatement("$T.requireNonNull(val.toString(), $S)", OBJECTS, errMsg);
            methodBuilder.beginControlFlow("if (val.toString().isBlank())")
                .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, errMsg)
                .endControlFlow();
        } else {
            methodBuilder.beginControlFlow(CHK_NOT_NULL_OR_BLANK, OBJECTS, OBJECTS);
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
        final XmlAttributePrism prism = xmlAttributePrism.orElseThrow();
        final boolean required = Boolean.TRUE.equals(prism.required());
        final String attributeName = attributeName(analysedComponent, prism);
        final Optional<String> namespaceName = namespaceName(prism);

        if (required) {
            final String errMsg = XML_CANNOT_NULL_REQUIRED_ATTRIBUTE.formatted(analysedComponent.name(), attributeName);
            methodBuilder.addStatement("$T.requireNonNull(val, $S)", OBJECTS, errMsg);
            methodBuilder.addStatement("$T.requireNonNull(val.toString(), $S)", OBJECTS, errMsg);
            methodBuilder.beginControlFlow("if (val.toString().isBlank())")
                .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, errMsg)
                .endControlFlow();
        } else {
            methodBuilder.beginControlFlow(CHK_NOT_NULL_OR_BLANK, OBJECTS, OBJECTS);
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
