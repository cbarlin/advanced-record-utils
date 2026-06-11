package io.github.cbarlin.aru.impl.xml;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.xml.iface.WriteIfaceToXml;
import io.github.cbarlin.aru.impl.xml.utils.WriteStaticToXml;
import io.github.cbarlin.aru.prism.prison.XmlRootElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlSchemaPrism;
import io.github.cbarlin.aru.prism.prison.XmlTypePrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_DEFAULT_NAMESPACE_VAR_NAME;

import static io.github.cbarlin.aru.impl.Constants.Names.STRING;

public sealed abstract class ToXmlMethod extends XmlVisitor permits WriteIfaceToXml, WriteStaticToXml {

    protected static final TypeName NULLABLE_STRING = STRING.withoutAnnotations().annotated(CommonsConstants.NULLABLE_ANNOTATION);

    protected final Optional<XmlRootElementPrism> xmlRootElementPrism;
    protected final Optional<XmlTypePrism> xmlTypePrism;
    protected final MethodSpec.Builder interfaceMethod;
    protected final MethodSpec.Builder staticMethod;
    protected final List<AnalysedComponent> components = new ArrayList<>();
    protected final Set<AnalysedComponent> elComponents = new HashSet<>();
    protected final Set<AnalysedComponent> attrComponents = new HashSet<>();

    protected ToXmlMethod(
        final ClaimableOperation claimableOperation,
        final XmlRecordHolder xmlRecordHolder,
        final Optional<XmlTypePrism> xmlTypePrism,
        final Optional<XmlRootElementPrism> xmlRootElementPrism
    ) {
        super(claimableOperation, xmlRecordHolder);
        this.xmlTypePrism = xmlTypePrism;
        this.xmlRootElementPrism = xmlRootElementPrism;
        this.interfaceMethod = xmlRecordHolder.interfaceToXml();
        this.staticMethod = xmlRecordHolder.staticToXml();
    }

    @Override
    protected final boolean visitStartOfClassImpl() {
        return true;
    }

    @Override
    protected final boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        components.add(analysedComponent);
        if (holder.xmlAttributeMapper().optionalInstanceOn(analysedComponent.element()).isPresent()) {
            attrComponents.add(analysedComponent);
        } else if (holder.xmlTransientMapper().optionalInstanceOn(analysedComponent.element()).isEmpty()) {
            elComponents.add(analysedComponent);
        }
        return false;
    }

    protected void writeChangeDefaultNamespace(final MethodSpec.Builder methodBuilder) {
        final ClassName xmlUtilCn = xmlStaticClass.className();
        if (xmlRootElementPrism.isPresent()) {
            methodBuilder.beginControlFlow("if ($T.$L != null) ", xmlUtilCn, XML_DEFAULT_NAMESPACE_VAR_NAME)
            .addStatement(
                "output.setDefaultNamespace($T.$L)", xmlUtilCn, XML_DEFAULT_NAMESPACE_VAR_NAME
            )
            .endControlFlow()
            .addStatement(
                "final $T namespaceToPassDown = ($T.$L != null) ? $T.$L : currentDefaultNamespace", NULLABLE_STRING, xmlUtilCn, XML_DEFAULT_NAMESPACE_VAR_NAME, xmlUtilCn, XML_DEFAULT_NAMESPACE_VAR_NAME
            );

        } else {
            methodBuilder.addStatement(
                "final $T defNs = ($T.$L != null && (requestedNamespace != null) && (!requestedNamespace.isBlank())) ? $T.$L : null", NULLABLE_STRING, xmlUtilCn, XML_DEFAULT_NAMESPACE_VAR_NAME, xmlUtilCn, XML_DEFAULT_NAMESPACE_VAR_NAME
            )
            .beginControlFlow("if (defNs != null)")
            .addStatement(
                "output.setDefaultNamespace(defNs)"
            )
            .endControlFlow()
            .addStatement(
                "final $T namespaceToPassDown = defNs != null ? defNs : currentDefaultNamespace", NULLABLE_STRING
            );
        }
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
        final List<AnalysedComponent> components,
        final List<AnalysedComponent> writeOrder
    ) {
        if (isAlphabeticalAccessOrder) {
            final List<AnalysedComponent> toBeOrdered = new ArrayList<>();
            for(final AnalysedComponent component : components) {
                if (elComponents.contains(component) && (!(writeOrder.contains(component) || toBeOrdered.contains(component)))) {
                    toBeOrdered.add(component);
                }
            }
            toBeOrdered.sort(Comparator.comparing(AnalysedComponent::name));
            writeOrder.addAll(toBeOrdered);
        } else {
            // This will be declaration order then
            for(final AnalysedComponent component : components) {
                if (elComponents.contains(component) && (!writeOrder.contains(component))) {
                    writeOrder.add(component);
                }
            }
        }
    }

    protected final void manuallyOrderedElements(
        final List<AnalysedComponent> components, 
        final List<String> propOrder,
        final List<AnalysedComponent> writeOrder
    ) {
        for (final String prop : propOrder) {
            for(final AnalysedComponent component : components) {
                if (prop.equals(component.name()) && (!writeOrder.contains(component)) && elComponents.contains(component)) {
                    writeOrder.add(component);
                }
            }
        }
    }

    protected final void remainingAttributes(
        final List<AnalysedComponent> components,
        final List<AnalysedComponent> writeOrder
    ) {
        if (isAlphabeticalAccessOrder) {
            final List<AnalysedComponent> toBeOrdered = new ArrayList<>();
            for(final AnalysedComponent component : components) {
                if (attrComponents.contains(component) && !(writeOrder.contains(component) || toBeOrdered.contains(component))) {
                    toBeOrdered.add(component);
                }
                
            }
            toBeOrdered.sort(Comparator.comparing(AnalysedComponent::name));
            writeOrder.addAll(toBeOrdered);
        } else {
            // This will be declaration order then
            for(final AnalysedComponent component : components) {
                if (attrComponents.contains(component) && (!writeOrder.contains(component))) {
                    writeOrder.add(component);
                }
            }
        }
    }

    protected final void manuallyOrderedAttributes(
        final List<AnalysedComponent> components, 
        final List<String> propOrder,
        final List<AnalysedComponent> writeOrder
    ) {
        for (final String prop : propOrder) {
            for(final AnalysedComponent component : components) {
                if (prop.equals(component.name()) && (!writeOrder.contains(component)) && attrComponents.contains(component)) {
                    writeOrder.add(component);
                }
            }
        }
    }

    protected final void addWriteOrderComment(final MethodSpec.Builder methodBuilder) {
        if (isAlphabeticalAccessOrder) {
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
