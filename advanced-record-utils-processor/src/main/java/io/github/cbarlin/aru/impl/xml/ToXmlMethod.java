package io.github.cbarlin.aru.impl.xml;

import static io.github.cbarlin.aru.impl.Constants.Names.XML_ATTRIBUTE;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_ELEMENT;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_ELEMENTS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.TypeElement;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementsPrism;
import io.github.cbarlin.aru.prism.prison.XmlRootElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlSchemaPrism;

import io.micronaut.sourcegen.javapoet.MethodSpec;

public abstract class ToXmlMethod extends XmlVisitor {

    protected ToXmlMethod(final ClaimableOperation claimableOperation) {
        super(claimableOperation);
    }

    protected final void configureNamespaceContext(final AnalysedRecord analysedRecord, final MethodSpec.Builder methodBuilder) {
        final TypeElement typeElement = analysedRecord.typeElement();
        if (XmlRootElementPrism.isPresent(typeElement)) {
            methodBuilder.addJavadoc("\n<p>\n")
                .addJavadoc("As this is a root element, it will configure the default namespace and other prefixes");
            final Set<String> usedPrefixes = new HashSet<>();
            final Set<String> usedUris = new HashSet<>();

            XmlSchemaPrism.getOptionalOn(analysedRecord.typeElement().getEnclosingElement())
                .map(XmlSchemaPrism::xmlns)
                .orElse(List.of())
                .stream()
                .filter(xmlNsPrism -> !(usedPrefixes.contains(xmlNsPrism.prefix()) || usedUris.contains(xmlNsPrism.namespaceURI())))
                .forEach(
                    xmlNsPrism -> {
                        usedPrefixes.add(xmlNsPrism.prefix());
                        usedUris.add(xmlNsPrism.namespaceURI());
                        methodBuilder.addStatement("output.setPrefix($S, $S)", xmlNsPrism.prefix(), xmlNsPrism.namespaceURI());
                    }
                );
        }
    }

    protected final void remainingElements(
        final AnalysedRecord analysedRecord, 
        final String accessOrder,
        final List<AnalysedComponent> writeOrder
    ) {
        if ("ALPHABETICAL".equals(accessOrder)) {
            final List<AnalysedComponent> toBeOrdered = new ArrayList<>();
            for(final AnalysedComponent component : analysedRecord.components()) {
                if (component.isPrismPresent(XML_ELEMENT, XmlElementPrism.class) || component.isPrismPresent(XML_ELEMENTS, XmlElementsPrism.class)) {
                    if (!(writeOrder.contains(component) || toBeOrdered.contains(component))) {
                        toBeOrdered.add(component);
                    }
                }
            }
            toBeOrdered.sort((a, b) -> a.name().compareTo(b.name()));
            writeOrder.addAll(toBeOrdered);
        } else {
            // This will be declaration order then
            for(final AnalysedComponent component : analysedRecord.components()) {
                if (component.isPrismPresent(XML_ELEMENT, XmlElementPrism.class) || component.isPrismPresent(XML_ELEMENTS, XmlElementsPrism.class)) {
                    if (!writeOrder.contains(component)) {
                        writeOrder.add(component);
                    }
                }
            }
        }
    }

    protected final void manuallyOrderedElements(
        final AnalysedRecord analysedRecord, 
        final List<String> propOrder,
        final List<AnalysedComponent> writeOrder
    ) {
        for (final String prop : propOrder) {
            for(final AnalysedComponent component : analysedRecord.components()) {
                if (prop.equals(component.name()) && (!writeOrder.contains(component)) && (component.isPrismPresent(XML_ELEMENT, XmlElementPrism.class) || component.isPrismPresent(XML_ELEMENTS, XmlElementsPrism.class))) {
                    writeOrder.add(component);
                }
            }
        }
    }

    protected final void remainingAttributes(
        final AnalysedRecord analysedRecord, 
        final String accessOrder,
        final List<AnalysedComponent> writeOrder
    ) {
        if ("ALPHABETICAL".equals(accessOrder)) {
            final List<AnalysedComponent> toBeOrdered = new ArrayList<>();
            for(final AnalysedComponent component : analysedRecord.components()) {
                if (component.isPrismPresent(XML_ATTRIBUTE, XmlAttributePrism.class) && !(writeOrder.contains(component) || toBeOrdered.contains(component))) {
                    toBeOrdered.add(component);
                }
                
            }
            toBeOrdered.sort((a, b) -> a.name().compareTo(b.name()));
            writeOrder.addAll(toBeOrdered);
        } else {
            // This will be declaration order then
            for(final AnalysedComponent component : analysedRecord.components()) {
                if (component.isPrismPresent(XML_ATTRIBUTE, XmlAttributePrism.class) && (!writeOrder.contains(component))) {
                    writeOrder.add(component);
                }
            }
        }
    }

    protected final void manuallyOrderedAttributes(
        final AnalysedRecord analysedRecord, 
        final List<String> propOrder,
        final List<AnalysedComponent> writeOrder
    ) {
        for (final String prop : propOrder) {
            for(final AnalysedComponent component : analysedRecord.components()) {
                if (prop.equals(component.name()) && (!writeOrder.contains(component)) && component.isPrismPresent(XML_ATTRIBUTE, XmlAttributePrism.class)) {
                    writeOrder.add(component);
                }
            }
        }
    }

    protected final void addWriteOrderComment(final MethodSpec.Builder methodBuilder, final String accessOrder) {
        if ("ALPHABETICAL".equals(accessOrder)) {
            methodBuilder.addComment("The write order is as follows:")
                .addComment(" 1. Attributes in the propOrder if present")
                .addComment(" 2. Remaining attributes in alphabetical order")
                .addComment(" 3. Element or Elements in the propOrder if present")
                .addComment(" 4. Element or Elements in alphabetical order");
        } else {
            methodBuilder.addComment("The write order is as follows:")
                .addComment(" 1. Attributes in the propOrder if present")
                .addComment(" 2. Remaining attributes in declaration order")
                .addComment(" 3. Element or Elements in the propOrder if present")
                .addComment(" 4. Element or Elements in declaration order");
        }
    }

}
