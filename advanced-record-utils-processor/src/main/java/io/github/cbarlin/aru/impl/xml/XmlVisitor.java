package io.github.cbarlin.aru.impl.xml;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlOptionsPrism;
import io.github.cbarlin.aru.prism.prison.XmlRootElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlSchemaPrism;
import io.github.cbarlin.aru.prism.prison.XmlTypePrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_DEFAULT_STRING;
import static io.github.cbarlin.aru.impl.Constants.Names.STRING;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_WRITER;

public abstract class XmlVisitor extends RecordVisitor {

    // Uhuh...
    private static final String XML_ELEMENT_DEFAULT_VALUE_NULLISH_MARKER = "\u0000";

    protected static final String STATIC_WRITE_XML_NAME = XmlClassFactory.STATIC_WRITE_XML_NAME;
    protected static final String XML_CANNOT_NULL_REQUIRED_ATTRIBUTE = "Attribute is marked as required - cannot provide a null/blank value for field %s (attribute name: %s)";
    protected static final String XML_CANNOT_NULL_REQUIRED_ELEMENT = "Element is marked as required - cannot provide a null/blank value for field %s (element name: %s)";
    protected final ToBeBuilt xmlInterface;
    protected final ToBeBuilt xmlStaticClass;
    protected final XmlOptionsPrism xmlOptionsPrism;
    protected final Optional<String> namespace;
    protected final String name;
    protected final boolean isAlphabeticalAccessOrder;
    protected final XmlRecordHolder holder;

    protected XmlVisitor(final ClaimableOperation claimableOperation, final XmlRecordHolder xmlHolder) {
        super(claimableOperation, xmlHolder.analysedRecord());
        this.xmlInterface = xmlHolder.xmlInterface();
        this.xmlStaticClass = xmlHolder.xmlStaticClass();
        this.xmlOptionsPrism = xmlHolder.prism();
        this.namespace = xmlHolder.namespace();
        this.name = xmlHolder.elementName();
        this.isAlphabeticalAccessOrder = xmlHolder.isAlphabeticalAccessOrder();
        this.holder = xmlHolder;
    }

    protected abstract int innerSpecificity();

    @Override
    public final int specificity() {
        return 1 + innerSpecificity();
    }

    protected Optional<XmlAttributePrism> xmlAttributePrism(final AnalysedComponent analysedComponent) {
        return holder.xmlAttributeMapper().optionalInstanceOn(analysedComponent.element());
    }

    protected String attributeName(final AnalysedComponent analysedComponent, final XmlAttributePrism prism) {
        return Optional.ofNullable(prism.name())
            .filter(StringUtils::isNotBlank)
            .filter(Predicate.not(XML_DEFAULT_STRING::equals))
            .orElseGet(analysedComponent::name);
    }

    protected Optional<String> namespaceName(final XmlAttributePrism prism) {
        return Optional.ofNullable(prism.namespace())
            .filter(StringUtils::isNotBlank)
            .filter(Predicate.not(XML_DEFAULT_STRING::equals));
    }

    protected String findElementName(final AnalysedComponent analysedComponent, final XmlElementPrism prism) {
        return Optional.ofNullable(prism.name())
            .filter(StringUtils::isNotBlank)
            .filter(Predicate.not(XML_DEFAULT_STRING::equals))
            .orElseGet(analysedComponent::name);
    }

    protected Optional<String> namespaceName(final XmlElementPrism prism) {
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
