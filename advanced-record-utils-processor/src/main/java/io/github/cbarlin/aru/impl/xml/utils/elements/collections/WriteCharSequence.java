package io.github.cbarlin.aru.impl.xml.utils.elements.collections;

import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_DEFAULT_STRING;
import static io.github.cbarlin.aru.impl.Constants.Names.CHAR_SEQUENCE;
import static io.github.cbarlin.aru.impl.Constants.Names.ITERABLE;
import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;

import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementWrapperPrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public class WriteCharSequence extends XmlVisitor {

    public WriteCharSequence() {
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
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        final Optional<XmlElementPrism> optPrism = XmlElementPrism.getOptionalOn(analysedComponent.element().getAccessor());
        if (isSupported(analysedComponent, optPrism)) {
            final XmlElementPrism prism = optPrism.get();
            final String elementName = elementName(analysedComponent, prism);
            final boolean required = Boolean.TRUE.equals(prism.required());
            final Optional<String> defaultValue = defaultValue(prism);
            final Optional<String> namespaceName = namespaceName(analysedComponent, prism);
            final MethodSpec.Builder methodBuilder = createMethod(analysedComponent, analysedComponent.typeName());

            if (defaultValue.isPresent()) {
                APContext.messager().printWarning("Cannot process default value on an XmlElement that's pointing at a collection", analysedComponent.element());
            }

            final Optional<XmlElementWrapperPrism> wrapper = XmlElementWrapperPrism.getOptionalOn(analysedComponent.element().getAccessor());
            if (wrapper.isPresent()) {
                handleWrapperStart(analysedComponent, elementName, required, methodBuilder, wrapper.get());
            }

            methodBuilder.beginControlFlow("for (final $T v : val)", CHAR_SEQUENCE)
                .beginControlFlow("if ($T.isNull(v))", OBJECTS)
                .addStatement("continue")
                .endControlFlow();
                namespaceName.ifPresentOrElse(
                    namespace -> methodBuilder.addStatement("output.writeStartElement($S, $S)", namespace, elementName),
                    () -> methodBuilder.addStatement("output.writeStartElement($S)", elementName)
                );
                methodBuilder.addStatement("output.writeCharacters(v.toString())")
                    .addStatement("output.writeEndElement()");
            methodBuilder.endControlFlow();

            if (wrapper.isPresent()) {
                handleWrapperEnd(required, methodBuilder);
            }

            return true;
        }

        return false;
    }

    private boolean isSupported(AnalysedComponent analysedComponent, final Optional<XmlElementPrism> optPrism) {
        final var types = APContext.types();
        return optPrism.isPresent() && 
            analysedComponent.requiresUnwrapping() && 
            types.isAssignable(
                analysedComponent.unNestedPrimaryComponentType(), 
                APContext.elements().getTypeElement(CHAR_SEQUENCE.canonicalName()).asType()
            ) &&
            types.isAssignable(
                types.erasure(analysedComponent.componentType()), 
                APContext.elements().getTypeElement(ITERABLE.canonicalName()).asType()
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
