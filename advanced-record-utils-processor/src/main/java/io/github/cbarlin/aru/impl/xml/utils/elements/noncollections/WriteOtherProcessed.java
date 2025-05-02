package io.github.cbarlin.aru.impl.xml.utils.elements.noncollections;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_UTILS_CLASS;

import java.util.Optional;

import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;

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

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final Optional<XmlElementPrism> optPrism = xmlElementPrism(analysedComponent);
        final Optional<ProcessingTarget> targetAnalysedType = analysedComponent.targetAnalysedType();
        if (optPrism.isPresent() && (!analysedComponent.requiresUnwrapping()) && targetAnalysedType.isPresent() && targetAnalysedType.get() instanceof final AnalysedRecord other) {
            final XmlElementPrism prism = optPrism.get();
            final String elementName = elementName(analysedComponent, prism);
            final boolean required = Boolean.TRUE.equals(prism.required());
            final Optional<String> defaultValue = defaultValue(prism);
            final Optional<String> namespaceName = namespaceName(prism);
            final MethodSpec.Builder methodBuilder = createMethod(analysedComponent, analysedComponent.typeName());
            analysedComponent.addCrossReference(other);

            if (defaultValue.isPresent()) {
                APContext.messager().printWarning("Cannot process default value on an XmlElement that's pointing at a complex type", analysedComponent.element());
            }

            if (required) {
                final String errMsg = XML_CANNOT_NULL_REQUIRED_ELEMENT.formatted(analysedComponent.name(), elementName);
                methodBuilder.addStatement("$T.requireNonNull(val, $S)", OBJECTS, errMsg);
            } else {
                methodBuilder.beginControlFlow("if ($T.nonNull(val))", OBJECTS);
            }

            final ClassName otherXmlUtils = other.utilsClassChildClass(XML_UTILS_CLASS, Claims.XML_STATIC_CLASS).className();

            namespaceName.ifPresentOrElse(
                namespace -> methodBuilder.addStatement("$T.$L(val, output, $S, $S, currentDefaultNamespace)", otherXmlUtils, STATIC_WRITE_XML_NAME, elementName, namespace),
                () -> methodBuilder.addStatement("$T.$L(val, output, $S, null, currentDefaultNamespace)", otherXmlUtils, STATIC_WRITE_XML_NAME, elementName)
            );

            if (!required) {
                methodBuilder.endControlFlow();
            }

            return true;
        }
        return false;
    }
}
