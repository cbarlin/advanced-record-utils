package io.github.cbarlin.aru.impl.xml.utils.elements.noncollections;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedOptionalComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.XmlPerComponentScope;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import jakarta.inject.Singleton;

import java.util.Optional;

import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.UUID;

@Singleton
@XmlPerComponentScope
@RequiresBean({XmlElementPrism.class})
public final class WriteUUID extends NonCollectionXmlVisitor {

    private final XmlElementPrism prism;

    public WriteUUID(final XmlRecordHolder xmlRecordHolder, final XmlElementPrism prism, final Optional<AnalysedOptionalComponent> analysedOptionalComponent) {
        super(Claims.XML_WRITE_FIELD, xmlRecordHolder, analysedOptionalComponent);
        this.prism = prism;
    }

    @Override
    protected int innerSpecificity() {
        return 1;
    }

    @Override
    protected boolean writeElementMethod(AnalysedComponent analysedComponent) {
        if (UUID.equals(analysedComponent.serialisedTypeName())) {
            final String elementName = findElementName(analysedComponent, prism);
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
