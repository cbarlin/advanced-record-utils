package io.github.cbarlin.aru.impl.xml.utils.elements.collections;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedOptionalComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.ComponentTargetingRecord;
import io.github.cbarlin.aru.impl.wiring.XmlPerComponentScope;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementWrapperPrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.function.Predicate;

import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_DEFAULT_STRING;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_UTILS_CLASS;
import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;

@Singleton
@XmlPerComponentScope
@RequiresBean({XmlElementPrism.class, ComponentTargetingRecord.class, AnalysedCollectionComponent.class})
public final class WriteOtherProcessed extends XmlVisitor {

    private final XmlElementPrism prism;
    private final Optional<XmlElementWrapperPrism> wrapper;
    private final AnalysedCollectionComponent component;
    private final AnalysedRecord other;
    private final Optional<AnalysedOptionalComponent> optComponent;

    public WriteOtherProcessed (
        final XmlRecordHolder xmlRecordHolder,
        final XmlElementPrism prism,
        final Optional<XmlElementWrapperPrism> wrapper,
        final AnalysedCollectionComponent component,
        final ComponentTargetingRecord targetingRecord,
        final Optional<AnalysedOptionalComponent> optComponent
    ) {
        super(Claims.XML_WRITE_FIELD, xmlRecordHolder);
        this.prism = prism;
        this.wrapper = wrapper;
        this.component = component;
        this.other = targetingRecord.target();
        this.optComponent = optComponent;
    }

    @Override
    protected int innerSpecificity() {
        return 4;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final String elementName = findElementName(analysedComponent, prism);
        final boolean required = Boolean.TRUE.equals(prism.required());
        final Optional<String> defaultValue = defaultValue(prism);
        final Optional<String> namespaceName = namespaceName(prism);
        final MethodSpec.Builder methodBuilder = createMethod(component, component.typeName());
        final ClassName otherXmlUtils = other.utilsClassChildClass(XML_UTILS_CLASS, Claims.XML_STATIC_CLASS).className();
        analysedComponent.addCrossReference(other);

        if (defaultValue.isPresent()) {
            APContext.messager().printWarning("Cannot process default value on an XmlElement that's pointing at a complex type", component.element());
        }

        wrapper.ifPresent(xmlElementWrapperPrism -> handleWrapperStart(component, elementName, required, methodBuilder, xmlElementWrapperPrism));

        component.withinUnwrapped(
            variableName -> {
                methodBuilder.beginControlFlow("if($T.isNull($L))", OBJECTS, variableName)
                    .addStatement("continue")
                    .endControlFlow();
                namespaceName.ifPresentOrElse(
                    namespace -> methodBuilder.addStatement("$T.$L($L, output, $S, $S, currentDefaultNamespace)", otherXmlUtils, STATIC_WRITE_XML_NAME, variableName, elementName, namespace),
                    () -> methodBuilder.addStatement("$T.$L($L, output, $S, null, currentDefaultNamespace)", otherXmlUtils, STATIC_WRITE_XML_NAME, variableName, elementName)
                );
            },
            methodBuilder,
            "val"
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
