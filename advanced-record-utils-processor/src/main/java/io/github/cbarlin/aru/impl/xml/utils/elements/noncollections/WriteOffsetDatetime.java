package io.github.cbarlin.aru.impl.xml.utils.elements.noncollections;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.DATE_TIME_FORMATTER;
import static io.github.cbarlin.aru.impl.Constants.Names.OFFSET_DATE_TIME;
import static io.github.cbarlin.aru.impl.Constants.Names.ZONE_OFFSET;

import java.util.Optional;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public class WriteOffsetDatetime extends XmlVisitor {

    private static final String CHK_NON_NULL = "if ($T.nonNull(val))";

    public WriteOffsetDatetime() {
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
        if (optPrism.isPresent() && OFFSET_DATE_TIME.equals(analysedComponent.typeName())) {
            // Nice!
            final XmlElementPrism prism = optPrism.get();
            final String elementName = elementName(analysedComponent, prism);
            final boolean required = Boolean.TRUE.equals(prism.required());
            final Optional<String> defaultValue = defaultValue(prism);
            final Optional<String> namespaceName = namespaceName(prism);
            final MethodSpec.Builder methodBuilder = createMethod(analysedComponent, analysedComponent.typeName());
            
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
            methodBuilder.beginControlFlow(CHK_NON_NULL, OBJECTS);
        }
      
        namespaceName.ifPresentOrElse(
            namespace -> methodBuilder.addStatement("output.writeStartElement($S, $S)", namespace, elementName),
            () -> methodBuilder.addStatement("output.writeStartElement($S)", elementName)
        );
        methodBuilder.addStatement("output.writeCharacters(val.atZoneSameInstant($T.UTC).format($T.ISO_OFFSET_DATE_TIME))", ZONE_OFFSET, DATE_TIME_FORMATTER)
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
        methodBuilder.beginControlFlow(CHK_NON_NULL, OBJECTS)
            .addStatement("output.writeCharacters(val.atZoneSameInstant($T.UTC).format($T.ISO_OFFSET_DATE_TIME))", ZONE_OFFSET, DATE_TIME_FORMATTER)
            .nextControlFlow("else");
        logTrace(methodBuilder, "Supplied value for %s (element name %s) was null/blank, writing default of %s".formatted(analysedComponent.name(), elementName, writeAsDefaultValue));
        methodBuilder.addStatement("output.writeCharacters($S)", writeAsDefaultValue)
            .endControlFlow()
            .addStatement("output.writeEndElement()");
    }
}
