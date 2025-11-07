package io.github.cbarlin.aru.impl.xml.utils.elements.noncollections;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedOptionalComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.XmlPerComponentScope;
import io.github.cbarlin.aru.impl.xml.LibraryLoadedXmlStaticHelper;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import jakarta.inject.Singleton;

import java.util.Optional;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

@Singleton
@XmlPerComponentScope
@RequiresBean({XmlElementPrism.class, LibraryLoadedXmlStaticHelper.class})
public final class WriteLibraryLoaded extends NonCollectionXmlVisitor {

    private final XmlElementPrism prism;
    private final LibraryLoadedXmlStaticHelper other;

    public WriteLibraryLoaded(
        final XmlRecordHolder xmlRecordHolder,
        final XmlElementPrism prism,
        final LibraryLoadedXmlStaticHelper component,
        final Optional<AnalysedOptionalComponent> analysedOptionalComponent
    ) {
        super(Claims.XML_WRITE_FIELD, xmlRecordHolder, analysedOptionalComponent);
        this.prism = prism;
        this.other = component;
    }

    @Override
    protected int innerSpecificity() {
        return 3;
    }

    @Override
    protected boolean writeElementMethod(final AnalysedComponent analysedComponent) {
        final String elementName = findElementName(analysedComponent, prism);
        final boolean required = Boolean.TRUE.equals(prism.required());
        final Optional<String> defaultValue = defaultValue(prism);
        final Optional<String> namespaceName = namespaceName(prism);
        final MethodSpec.Builder methodBuilder = createMethod(analysedComponent, analysedComponent.serialisedTypeName());
        analysedComponent.addCrossReference(other.libraryLoadedTarget());

        if (defaultValue.isPresent()) {
            APContext.messager().printWarning("Cannot process default value on an XmlElement that's pointing at a complex type", analysedComponent.element());
        }

        if (required) {
            final String errMsg = XML_CANNOT_NULL_REQUIRED_ELEMENT.formatted(analysedComponent.name(), elementName);
            methodBuilder.addStatement("$T.requireNonNull(val, $S)", OBJECTS, errMsg);
        } else {
            methodBuilder.beginControlFlow("if ($T.nonNull(val))", OBJECTS);
        }

        final ClassName otherXmlUtils = other.helperClassName();

        namespaceName.ifPresentOrElse(
            namespace -> methodBuilder.addStatement("$T.$L(val, output, $S, $S, currentDefaultNamespace)", otherXmlUtils, STATIC_WRITE_XML_NAME, elementName, namespace),
            () -> methodBuilder.addStatement("$T.$L(val, output, $S, null, currentDefaultNamespace)", otherXmlUtils, STATIC_WRITE_XML_NAME, elementName)
        );

        if (!required) {
            methodBuilder.endControlFlow();
        }

        return true;
    }
}
