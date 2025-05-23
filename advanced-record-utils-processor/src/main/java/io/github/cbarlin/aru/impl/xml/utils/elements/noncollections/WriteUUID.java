package io.github.cbarlin.aru.impl.xml.utils.elements.noncollections;

import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.UUID;

import java.util.Optional;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;

import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public class WriteUUID extends XmlVisitor {

    public WriteUUID() {
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
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        final Optional<XmlElementPrism> optPrism = xmlElementPrism(analysedComponent);
        if (UUID.equals(analysedComponent.typeName()) && optPrism.isPresent()) {
            final XmlElementPrism prism = optPrism.get();
            final String elementName = elementName(analysedComponent, prism);
            final boolean required = Boolean.TRUE.equals(prism.required());
            final Optional<String> defaultValue = defaultValue(prism);
            final Optional<String> namespaceName = namespaceName(prism);
            final MethodSpec.Builder methodBuilder = createMethod(analysedComponent, UUID);

            if (defaultValue.isPresent()) {
                APContext.messager().printWarning("There is no support for default UUID values", analysedComponent.element());
            }

            if (required) {
                final String errMsg = XML_CANNOT_NULL_REQUIRED_ELEMENT.formatted(analysedComponent.name(), elementName);
                methodBuilder.addStatement("$T.requireNonNull(val, $S)", OBJECTS, errMsg);
            } else {
                methodBuilder.beginControlFlow("if ($T.nonNull(val))", OBJECTS);
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

            return true;
        }
        return false;
    }
}
