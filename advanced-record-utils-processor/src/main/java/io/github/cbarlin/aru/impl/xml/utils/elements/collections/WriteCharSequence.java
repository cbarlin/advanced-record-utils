package io.github.cbarlin.aru.impl.xml.utils.elements.collections;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedOptionalComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.XmlPerComponentScope;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementWrapperPrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.function.Predicate;

import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_DEFAULT_STRING;
import static io.github.cbarlin.aru.impl.Constants.Names.CHAR_SEQUENCE;
import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;

@Singleton
@XmlPerComponentScope
@RequiresBean({XmlElementPrism.class, AnalysedCollectionComponent.class})
public final class WriteCharSequence extends XmlVisitor {

    private final XmlElementPrism prism;
    private final Optional<XmlElementWrapperPrism> wrapper;
    private final AnalysedCollectionComponent component;

    public WriteCharSequence (
        final XmlRecordHolder xmlRecordHolder,
        final XmlElementPrism prism,
        final Optional<XmlElementWrapperPrism> wrapper,
        final AnalysedCollectionComponent component,
        final Optional<AnalysedOptionalComponent> optComponent
    ) {
        super(Claims.XML_WRITE_FIELD, xmlRecordHolder);
        this.prism = prism;
        this.wrapper = wrapper;
        this.component = component;
    }

    @Override
    protected int innerSpecificity() {
        return 3;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        if(!OptionalClassDetector.checkSameOrSubType(component.unNestedPrimaryTypeName(), CHAR_SEQUENCE)) {
            return false;
        }
        final var optWrapperTypeName = component.erasedWrapperTypeName();
        if (optWrapperTypeName.isEmpty()) {
            return false;
        }
        final ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(optWrapperTypeName.get(), component.unNestedPrimaryTypeName());
        final String elementName = findElementName(component, prism);
        final boolean required = Boolean.TRUE.equals(prism.required());
        final Optional<String> defaultValue = defaultValue(prism);
        final Optional<String> namespaceName = namespaceName(prism);
        final MethodSpec.Builder methodBuilder = createMethod(component, parameterizedTypeName);

        if (defaultValue.isPresent()) {
            APContext.messager().printWarning("Cannot process default value on an XmlElement that's pointing at a collection", analysedComponent.element());
        }

        wrapper.ifPresent(xmlElementWrapperPrism -> handleWrapperStart(component, elementName, required, methodBuilder, xmlElementWrapperPrism));

        component.withinUnwrapped(
            variableName -> {
                methodBuilder.beginControlFlow("if($T.isNull($L))", OBJECTS, variableName)
                    .addStatement("continue")
                    .endControlFlow();
                namespaceName.ifPresentOrElse(
                    namespace -> methodBuilder.addStatement("output.writeStartElement($S, $S)", namespace, elementName),
                    () -> methodBuilder.addStatement("output.writeStartElement($S)", elementName)
                );
                methodBuilder.addStatement("output.writeCharacters($L.toString())", variableName)
                    .addStatement("output.writeEndElement()");
            },
            methodBuilder,
            "val",
            CHAR_SEQUENCE
        );

        if (wrapper.isPresent()) {
            handleWrapperEnd(required, methodBuilder);
        }

        return true;
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

    private void handleWrapperStart(final AnalysedComponent analysedComponent, final String elementName, final boolean required, final MethodSpec.Builder methodBuilder, final XmlElementWrapperPrism wrapperPrism) {
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
