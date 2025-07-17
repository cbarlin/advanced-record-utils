package io.github.cbarlin.aru.impl.xml.utils.elements.noncollections;

import static io.github.cbarlin.aru.impl.Constants.Names.ILLEGAL_ARGUMENT_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.Names.STRING;

import java.util.Optional;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.AnalysedOptionalPrimitiveComponent;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public final class WriteOptionalPrimitive extends XmlVisitor {

    public WriteOptionalPrimitive() {
        super(Claims.XML_WRITE_FIELD);
    }

    @Override
    protected int innerSpecificity() {
        return 2;
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final Optional<XmlElementPrism> optPrism = xmlElementPrism(analysedComponent);
        if (optPrism.isPresent() && analysedComponent instanceof final AnalysedOptionalPrimitiveComponent component) {
            final XmlElementPrism prism = optPrism.get();
            final String elementName = elementName(component, prism);
            final boolean required = Boolean.TRUE.equals(prism.required());
            final Optional<String> defaultValue = defaultValue(prism);
            final Optional<String> namespaceName = namespaceName(prism);
            final MethodSpec.Builder methodBuilder = createMethod(component, component.typeName());

            final String methodName = component.getterMethod();

            if (defaultValue.isPresent()) {
                final String writeAsDefaultValue = defaultValue.get();
                writeValueWithDefault(component, elementName, namespaceName, methodBuilder, methodName, writeAsDefaultValue);
            } else {
                writeValueWithoutDefault(component, elementName, required, namespaceName, methodBuilder, methodName);
            }

            return true;
        }
        return false;
    }

    private void writeValueWithoutDefault(final AnalysedOptionalPrimitiveComponent component, 
                                          final String elementName, 
                                          final boolean required,
                                          final Optional<String> namespaceName, 
                                          final MethodSpec.Builder methodBuilder, 
                                          final String methodName
    ) {
        if (required) {
            final String errMsg = XML_CANNOT_NULL_REQUIRED_ELEMENT.formatted(component.name(), elementName);
            methodBuilder.beginControlFlow("if (val.isEmpty())")
                .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, errMsg)
                .endControlFlow();
        } else {
            methodBuilder.beginControlFlow("if (val.isEmpty())")
                .addComment("$S", "No need to do anything")
                .addStatement("return")
                .endControlFlow();
        }
        namespaceName.ifPresentOrElse(
            namespace -> methodBuilder.addStatement("output.writeStartElement($S, $S)", namespace, elementName),
            () -> methodBuilder.addStatement("output.writeStartElement($S)", elementName)
        );
        methodBuilder.addStatement("output.writeCharacters($T.valueOf(val.$L()))", STRING, methodName)
            .addStatement("output.writeEndElement()");
    }

    private void writeValueWithDefault(final AnalysedOptionalPrimitiveComponent component, 
                                       final String elementName,
                                       final Optional<String> namespaceName, 
                                       final MethodSpec.Builder methodBuilder, 
                                       final String methodName,
                                       final String writeAsDefaultValue
    ) {
        namespaceName.ifPresentOrElse(
            namespace -> methodBuilder.addStatement("output.writeStartElement($S, $S)", namespace, elementName),
            () -> methodBuilder.addStatement("output.writeStartElement($S)", elementName)
        );
        methodBuilder.beginControlFlow("if (val.isPresent())")
            .addStatement("output.writeCharacters($T.valueOf(val.$L()))", STRING, methodName)
            .nextControlFlow("else");
        logTrace(methodBuilder, "Supplied value for %s (element name %s) was null/blank, writing default of %s".formatted(component.name(), elementName, writeAsDefaultValue));
        methodBuilder.addStatement("output.writeCharacters($S)", writeAsDefaultValue)
            .endControlFlow()
            .addStatement("output.writeEndElement()");
    }
}
