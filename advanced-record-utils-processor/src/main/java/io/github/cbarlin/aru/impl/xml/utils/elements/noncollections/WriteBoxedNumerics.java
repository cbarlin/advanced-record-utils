package io.github.cbarlin.aru.impl.xml.utils.elements.noncollections;

import static io.github.cbarlin.aru.impl.Constants.Names.BIG_DECIMAL;
import static io.github.cbarlin.aru.impl.Constants.Names.BIG_INTEGER;
import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;

import java.util.Optional;
import java.util.Set;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public class WriteBoxedNumerics extends XmlVisitor {

    private static final Set<TypeName> BOXED_NUMERICS = Set.of(
        TypeName.BYTE.box(),
        TypeName.CHAR.box(),
        TypeName.DOUBLE.box(),
        TypeName.FLOAT.box(),
        TypeName.INT.box(),
        TypeName.LONG.box(),
        TypeName.SHORT.box(),
        BIG_DECIMAL,
        BIG_INTEGER
    );

    public WriteBoxedNumerics() {
        super(Claims.XML_WRITE_FIELD);
    }

    @Override
    protected int innerSpecificity() {
        return 1;
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final Optional<XmlElementPrism> optPrism = xmlElementPrism(analysedComponent);
        if (optPrism.isPresent() && (!analysedComponent.isLoopable()) && BOXED_NUMERICS.contains(analysedComponent.unNestedPrimaryTypeName())) {
            final XmlElementPrism prism = optPrism.get();
            final String elementName = elementName(analysedComponent, prism);
            final boolean required = Boolean.TRUE.equals(prism.required());
            final Optional<String> defaultValue = defaultValue(prism);
            final Optional<String> namespaceName = namespaceName(prism);
            final MethodSpec.Builder methodBuilder = createMethod(analysedComponent, analysedComponent.unNestedPrimaryTypeName());
            if (defaultValue.isPresent()) {
                writeWithDefaultDefined(analysedComponent, elementName, defaultValue.get(), namespaceName, methodBuilder);
            } else {
                writeWithoutDefaultDefined(analysedComponent, elementName, required, namespaceName, methodBuilder);
            }

            return true;
        }
        return false;
    }

    private void writeWithoutDefaultDefined(
            final AnalysedComponent analysedComponent, 
            final String elementName, 
            final boolean required,
            final Optional<String> namespaceName, 
            final MethodSpec.Builder methodBuilder
    ) {
        if (required) {
            final String errMsg = XML_CANNOT_NULL_REQUIRED_ELEMENT.formatted(analysedComponent.name(), elementName);
            methodBuilder.addStatement("$T.requireNonNull(val, $S)", OBJECTS, errMsg);
        } else {
            methodBuilder.beginControlFlow("if($T.nonNull(val))", OBJECTS);
        }

        namespaceName.ifPresentOrElse(
            namespace -> methodBuilder.addStatement("output.writeStartElement($S, $S)", namespace, elementName),
            () -> methodBuilder.addStatement("output.writeStartElement($S)", elementName)
        );
        methodBuilder.addStatement("output.writeCharacters(val.toString())")
            .addStatement("output.writeEndElement()");
      
        if (!required) {
            methodBuilder.endControlFlow();
        }
    }

    private void writeWithDefaultDefined(
            final AnalysedComponent analysedComponent, 
            final String elementName,
            final String writeAsDefaultValue, 
            final Optional<String> namespaceName,
            final MethodSpec.Builder methodBuilder
    ) {
        namespaceName.ifPresentOrElse(
            namespace -> methodBuilder.addStatement("output.writeStartElement($S, $S)", namespace, elementName),
            () -> methodBuilder.addStatement("output.writeStartElement($S)", elementName)
        );
        methodBuilder.beginControlFlow("if($T.nonNull(val))", OBJECTS)
            .addStatement("output.writeCharacters(val.toString())")
            .nextControlFlow("else");
        logTrace(methodBuilder, "Supplied value for %s (element name %s) was null/blank, writing default of %s".formatted(analysedComponent.name(), elementName, writeAsDefaultValue));
        methodBuilder.addStatement("output.writeCharacters($S)", writeAsDefaultValue)
            .endControlFlow()
            .addStatement("output.writeEndElement()");
    }
}
