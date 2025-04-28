package io.github.cbarlin.aru.impl.xml.utils.elements.collections;

import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_DEFAULT_STRING;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_UTILS_CLASS;
import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;

import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.impl.types.AnalysedOptionalComponent;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementWrapperPrism;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public class WriteOtherProcessed extends XmlVisitor {

    public WriteOtherProcessed() {
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

    private final Optional<AnalysedRecord> extractSupported(final AnalysedComponent analysedComponent, final Optional<XmlElementPrism> optPrism) {
        final Optional<ProcessingTarget> targetAnalysedType = analysedComponent.targetAnalysedType();
        if(optPrism.isPresent() &&
           analysedComponent.requiresUnwrapping() &&
           (!(analysedComponent instanceof AnalysedOptionalComponent)) &&
           targetAnalysedType.isPresent() && 
           targetAnalysedType.get() instanceof final AnalysedRecord other
        ) {
            return Optional.of(other);
        }
        return Optional.empty();
    }


    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        final Optional<XmlElementPrism> optPrism = XmlElementPrism.getOptionalOn(analysedComponent.element().getAccessor());
        final var otherOpt = extractSupported(analysedComponent, optPrism);
        if (optPrism.isPresent() && otherOpt.isPresent()) {
            final XmlElementPrism prism = optPrism.get();
            final AnalysedRecord other = otherOpt.get();
            final String elementName = elementName(analysedComponent, prism);
            final boolean required = Boolean.TRUE.equals(prism.required());
            final Optional<String> defaultValue = defaultValue(prism);
            final Optional<String> namespaceName = namespaceName(analysedComponent, prism);
            final MethodSpec.Builder methodBuilder = createMethod(analysedComponent, analysedComponent.typeName());
            final ClassName otherXmlUtils = other.utilsClassChildClass(XML_UTILS_CLASS, Claims.XML_STATIC_CLASS).className();
            analysedComponent.addCrossReference(other);

            if (defaultValue.isPresent()) {
                APContext.messager().printWarning("Cannot process default value on an XmlElement that's pointing at a complex type", analysedComponent.element());
            }

            final Optional<XmlElementWrapperPrism> wrapper = XmlElementWrapperPrism.getOptionalOn(analysedComponent.element().getAccessor());
            if (wrapper.isPresent()) {
                handleWrapperStart(analysedComponent, elementName, required, methodBuilder, wrapper.get());
            }

            methodBuilder.beginControlFlow("for (final $T v : val)", other.intendedType())
                .beginControlFlow("if ($T.isNull(v))", OBJECTS)
                .addStatement("continue")
                .endControlFlow();
            namespaceName.ifPresentOrElse(
                namespace -> methodBuilder.addStatement("$T.$L(v, output, $S, $S, currentDefaultNamespace)", otherXmlUtils, STATIC_WRITE_XML_NAME, elementName, namespace),
                () -> methodBuilder.addStatement("$T.$L(v, output, $S, null, currentDefaultNamespace)", otherXmlUtils, STATIC_WRITE_XML_NAME, elementName)
            );
            methodBuilder.endControlFlow();

            if (wrapper.isPresent()) {
                handleWrapperEnd(required, methodBuilder);
            }

            return true;
        }

        return false;
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
