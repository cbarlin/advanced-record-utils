package io.github.cbarlin.aru.impl.xml.utils.elements.collections;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL_INT;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL_LONG;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_DEFAULT_STRING;
import static io.github.cbarlin.aru.impl.Constants.Names.STRING;

import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL_DOUBLE;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.AnalysedOptionalPrimitiveComponent;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementWrapperPrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public class WriteOptionalPrimitive extends XmlVisitor {

    public WriteOptionalPrimitive() {
        super(Claims.XML_WRITE_FIELD);
    }

    @Override
    protected int innerSpecificity() {
        return 3;
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        final Optional<XmlElementPrism> optPrism = xmlElementPrism(analysedComponent);
        if (optPrism.isPresent() && isSupported(analysedComponent)) {
            final XmlElementPrism prism = optPrism.get();
            final String elementName = elementName(analysedComponent, prism);
            final boolean required = Boolean.TRUE.equals(prism.required());
            final Optional<String> defaultValue = defaultValue(prism);
            final Optional<String> namespaceName = namespaceName(prism);
            final MethodSpec.Builder methodBuilder = createMethod(analysedComponent, analysedComponent.typeName());

            if (defaultValue.isPresent()) {
                APContext.messager().printWarning("Cannot process default value on an XmlElement that's pointing at a collection", analysedComponent.element());
            }
            
            final Optional<XmlElementWrapperPrism> wrapper = xmlElementWrapperPrism(analysedComponent);
            if (wrapper.isPresent()) {
                handleWrapperStart(analysedComponent, elementName, required, methodBuilder, wrapper.get());
            }

            final String methodName = AnalysedOptionalPrimitiveComponent.getterMethod(analysedComponent.typeName());

            analysedComponent.withinUnwrapped(
                variableName -> {
                    methodBuilder.beginControlFlow("if($T.isNull($L) || $L.isEmpty())", OBJECTS, variableName, variableName)
                        .addStatement("continue")
                        .endControlFlow();
                    namespaceName.ifPresentOrElse(
                        namespace -> methodBuilder.addStatement("output.writeStartElement($S, $S)", namespace, elementName),
                        () -> methodBuilder.addStatement("output.writeStartElement($S)", elementName)
                    );
                    methodBuilder.addStatement("output.writeCharacters($T.valueOf($L.$L()))", STRING, variableName, methodName)
                        .addStatement("output.writeEndElement()");
                },
                methodBuilder,
                "val"
            );

            if (wrapper.isPresent()) {
                handleWrapperEnd(required, methodBuilder);
            }
            return true;
        }
        return false;
    }

    private boolean isSupported(final AnalysedComponent analysedComponent) {
        return analysedComponent.isLoopable() &&
          (
            analysedComponent.unNestedPrimaryTypeName().equals(OPTIONAL_INT) ||
            analysedComponent.unNestedPrimaryTypeName().equals(OPTIONAL_LONG) ||
            analysedComponent.unNestedPrimaryTypeName().equals(OPTIONAL_DOUBLE)
          );
    }

    private void handleWrapperEnd(final boolean required, final MethodSpec.Builder methodBuilder) {
        methodBuilder.addStatement("output.writeEndElement()");
        if (!Boolean.TRUE.equals(xmlOptionsPrism.writeEmptyCollectionsWithWrapperAsEmptyElement())) {
            methodBuilder.endControlFlow();
            if (!required) {
                methodBuilder.endControlFlow();
            }
        }
    }

    private void handleWrapperStart(AnalysedComponent analysedComponent, final String elementName, final boolean required, final MethodSpec.Builder methodBuilder, final XmlElementWrapperPrism wrapperPrism) {
        if (!Boolean.TRUE.equals(xmlOptionsPrism.writeEmptyCollectionsWithWrapperAsEmptyElement())) {
            if (required) {
                final String errMsg = XML_CANNOT_NULL_REQUIRED_ELEMENT.formatted(analysedComponent.name(), elementName);
                methodBuilder.addStatement("$T.requireNonNull(val, $S)", OBJECTS, errMsg);
            } else {
                methodBuilder.beginControlFlow("if ($T.nonNull(val))", OBJECTS);
            }
            methodBuilder.beginControlFlow("if (!val.isEmpty())");
        }
        Optional.ofNullable(wrapperPrism.namespace())
            .filter(StringUtils::isNotBlank)
            .filter(Predicate.not(XML_DEFAULT_STRING::equals))
            .ifPresentOrElse(
                namespace1 -> methodBuilder.addStatement("output.writeStartElement($S, $S)", namespace1, wrapperPrism.name()),
                () -> methodBuilder.addStatement("output.writeStartElement($S)", wrapperPrism.name())
            );
    }
}
