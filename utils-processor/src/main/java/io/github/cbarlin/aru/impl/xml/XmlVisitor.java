package io.github.cbarlin.aru.impl.xml;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_DEFAULT_STRING;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_UTILS_CLASS;
import static io.github.cbarlin.aru.impl.Constants.Names.STRING;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_WRITER;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import org.apache.commons.lang3.StringUtils;

import io.github.cbarlin.aru.core.AdvancedRecordUtilsPrism.XmlOptionsPrism;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.prism.prison.XmlAccessorOrderPrism;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlRootElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlSchemaPrism;
import io.github.cbarlin.aru.prism.prison.XmlTypePrism;

import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

public abstract class XmlVisitor extends RecordVisitor {

    // Uhuh...
    private static final String XML_ELEMENT_DEFAULT_VALUE_NULLISH_MARKER = "\u0000";

    private static final Set<String> VALID_XML_ACCESS_ORDERS = Set.of("UNDEFINED", "ALPHABETICAL");

    protected static final String STATIC_WRITE_XML_NAME = "writeToXml";
    protected static final String XML_CANNOT_NULL_REQUIRED_ATTRIBUTE = "Attribute is marked as required - cannot provide a null/blank value for field %s (attribute name: %s)";
    protected static final String XML_CANNOT_NULL_REQUIRED_ELEMENT = "Element is marked as required - cannot provide a null/blank value for field %s (element name: %s)";
    protected ToBeBuilt xmlInterface;
    protected ToBeBuilt xmlStaticClass;
    protected XmlOptionsPrism xmlOptionsPrism;

    protected XmlVisitor(final ClaimableOperation claimableOperation) {
        super(claimableOperation);
    }

    protected abstract int innerSpecificity();

    @Override
    public final int specificity() {
        return 1 + innerSpecificity();
    }

    @Override
    public final boolean isApplicable(final AnalysedRecord analysedRecord) {
        if (Boolean.TRUE.equals(analysedRecord.settings().prism().xmlable())) {
            xmlOptionsPrism = analysedRecord.settings().prism().xmlOptions();
            final String xmlInterfaceName = xmlOptionsPrism.xmlName();
            xmlInterface = analysedRecord.utilsClass().childInterfaceArtifact(xmlInterfaceName, Claims.XML_IFACE);
            xmlStaticClass = analysedRecord.utilsClass().childClassArtifact(XML_UTILS_CLASS, Claims.XML_STATIC_CLASS);
            return innerIsApplicable(analysedRecord);
        }
        return false;
    }

    protected abstract boolean innerIsApplicable(final AnalysedRecord analysedRecord);

    protected String attributeName(final AnalysedComponent analysedComponent, final XmlAttributePrism prism) {
        return Optional.ofNullable(prism.name())
            .filter(StringUtils::isNotBlank)
            .filter(Predicate.not(XML_DEFAULT_STRING::equals))
            .orElseGet(analysedComponent::name);
    }

    protected Optional<String> namespaceName(final AnalysedComponent analysedComponent, final XmlAttributePrism prism) {
        return Optional.ofNullable(prism.namespace())
            .filter(StringUtils::isNotBlank)
            .filter(Predicate.not(XML_DEFAULT_STRING::equals));
    }

    protected String elementName(final AnalysedComponent analysedComponent, final XmlElementPrism prism) {
        return Optional.ofNullable(prism.name())
            .filter(StringUtils::isNotBlank)
            .filter(Predicate.not(XML_DEFAULT_STRING::equals))
            .orElseGet(analysedComponent::name);
    }

    protected Optional<String> namespaceName(final AnalysedComponent analysedComponent, final XmlElementPrism prism) {
        return Optional.ofNullable(prism.namespace())
            .filter(StringUtils::isNotBlank)
            .filter(Predicate.not(XML_DEFAULT_STRING::equals));
    }

    protected Optional<String> defaultValue(final XmlElementPrism prism) {
        return Optional.ofNullable(prism.defaultValue())
            .filter(StringUtils::isNotBlank)
            .filter(Predicate.not(XML_ELEMENT_DEFAULT_VALUE_NULLISH_MARKER::equals));
    }

    // I see us doing these a number of times, best to cache them
    private static final Map<TypeElement, Optional<String>> NAMESPACE_CACHE = new HashMap<>();
    private static final Map<TypeElement, String> ELEMENT_NAME_CACHE = new HashMap<>();
    private static final Map<AnalysedRecord, String> ACCESS_ORDER_CACHE = new HashMap<>();

    protected Optional<String> namespaceName(final AnalysedRecord analysedRecord) {
        return namespaceName(analysedRecord.typeElement());
    }

    protected Optional<String> namespaceName(final TypeElement typeElement) {
        return NAMESPACE_CACHE.computeIfAbsent(typeElement, XmlVisitor::namespaceNameImpl);
    }

    protected String elementName(final AnalysedRecord analysedRecord) {
        return elementName(analysedRecord.typeElement());
    }

    protected String elementName(final TypeElement typeElement) {
        return ELEMENT_NAME_CACHE.computeIfAbsent(typeElement, XmlVisitor::elementNameImpl);
    }

    protected String accessOrder(final AnalysedRecord analysedRecord) {
        return ACCESS_ORDER_CACHE.computeIfAbsent(analysedRecord, XmlVisitor::accessOrderImpl);
    }

    private static Optional<String> namespaceNameImpl(final TypeElement typeElement) {
        return XmlTypePrism.getOptionalOn(typeElement)
            .map(XmlTypePrism::namespace)
            .filter(StringUtils::isNotBlank)
            .filter(Predicate.not(XML_DEFAULT_STRING::equals))
            .or(
                () -> XmlRootElementPrism.getOptionalOn(typeElement)
                        .map(XmlRootElementPrism::namespace)
                        .filter(StringUtils::isNotBlank)
                        .filter(Predicate.not(XML_DEFAULT_STRING::equals))
            )
            .or(
                () -> XmlSchemaPrism.getOptionalOn(typeElement.getEnclosingElement())
                        .map(XmlSchemaPrism::namespace)   
                        .filter(StringUtils::isNotBlank)
                        .filter(Predicate.not(XML_DEFAULT_STRING::equals))
            );
    }

    private static String elementNameImpl(final TypeElement recordElement) {
        return XmlTypePrism.getOptionalOn(recordElement)
            .map(XmlTypePrism::name)
            .filter(StringUtils::isNotBlank)
            .filter(Predicate.not(XML_DEFAULT_STRING::equals))
            .or(
                () -> XmlRootElementPrism.getOptionalOn(recordElement)
                        .map(XmlRootElementPrism::name)
                        .filter(StringUtils::isNotBlank)
                        .filter(Predicate.not(XML_DEFAULT_STRING::equals))
            )
            .orElseGet(() -> recordElement.getSimpleName().toString());
    }

    private static String accessOrderImpl(final AnalysedRecord analysedRecord) {
        final TypeElement recordElement = analysedRecord.typeElement();
        return XmlAccessorOrderPrism.getOptionalOn(recordElement)
            .map(XmlAccessorOrderPrism::value)
            .filter(StringUtils::isNotBlank)
            .filter(VALID_XML_ACCESS_ORDERS::contains)
            .or(
                () -> XmlAccessorOrderPrism.getOptionalOn(recordElement.getEnclosingElement())
                        .map(XmlAccessorOrderPrism::value)
                        .filter(StringUtils::isNotBlank)
                        .filter(VALID_XML_ACCESS_ORDERS::contains)
            )
            .orElse("UNDEFINED");
    }

    protected final MethodSpec.Builder createMethod(final AnalysedComponent analysedComponent, final TypeName acceptedTypeName) {
        final MethodSpec.Builder methodBuilder = xmlStaticClass.createMethod(analysedComponent.name(), claimableOperation);
        methodBuilder.modifiers.clear();
        methodBuilder.addParameter(
                ParameterSpec.builder(XML_STREAM_WRITER, "output", Modifier.FINAL)
                    .addAnnotation(NON_NULL)
                    .addJavadoc("The output to write to")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(acceptedTypeName, "val", Modifier.FINAL)
                    .addAnnotation(NULLABLE)
                    .addJavadoc("The item to write")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(STRING, "currentDefaultNamespace", Modifier.FINAL)
                    .addAnnotation(NULLABLE)
                    .addJavadoc("The current default namespace")
                    .build()
            )
            .addException(XML_STREAM_EXCEPTION)
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
            .addJavadoc("Add the {@code $L} field to the XML output", analysedComponent.name());
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        return methodBuilder;
    }
}
