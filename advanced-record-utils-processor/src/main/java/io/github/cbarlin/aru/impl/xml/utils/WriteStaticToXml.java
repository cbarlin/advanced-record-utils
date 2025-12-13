package io.github.cbarlin.aru.impl.xml.utils;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.TypeAliasComponent;
import io.github.cbarlin.aru.impl.wiring.XmlPerRecordScope;
import io.github.cbarlin.aru.impl.xml.ToXmlMethod;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.prism.prison.XmlRootElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlSchemaPrism;
import io.github.cbarlin.aru.prism.prison.XmlTypePrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.ARU_GENERATED;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_DEFAULT_NAMESPACE_VAR_NAME;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_DEFAULT_STRING;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_DEFAULT_TAG_NAME_VAR_NAME;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_PACKAGE_NAMESPACE_VAR_NAME;
import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.PREDICATE;
import static io.github.cbarlin.aru.impl.Constants.Names.STRING;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_WRITER;

@Singleton
@XmlPerRecordScope
public final class WriteStaticToXml extends ToXmlMethod {

    private static final ParameterSpec CURR_DEF_NS_PARAM = ParameterSpec.builder(STRING.annotated(CommonsConstants.NULLABLE_ANNOTATION), "currentDefaultNamespace", Modifier.FINAL)
        .addJavadoc("The currently available namespace")
        .build();
    private static final ParameterSpec REQ_NS_PARAM = ParameterSpec.builder(STRING.annotated(CommonsConstants.NULLABLE_ANNOTATION), "requestedNamespace", Modifier.FINAL)
        .addJavadoc("The requested namespace when called")
        .build();
    private static final ParameterizedTypeName OPTIONAL_STRING = ParameterizedTypeName.get(OPTIONAL, STRING);

    public WriteStaticToXml(final XmlRecordHolder xmlRecordHolder, final Optional<XmlTypePrism> xmlTypePrism, final Optional<XmlRootElementPrism> xmlRootElementPrism) {
        super(Claims.XML_STATIC_CLASS_TO_XML, xmlRecordHolder, xmlTypePrism, xmlRootElementPrism);
    }

    @Override
    protected int innerSpecificity() {
        return 0;
    }

    @Override
    protected void visitEndOfClassImpl() {
        writeDefaultNamespaceVar(analysedRecord);
        writeDefaultTagVar(analysedRecord);
        writeCreateNamespace(analysedRecord);
        writeCreateTag();
        writeSelfToExistingWithTagAndNamespace(analysedRecord);
        
    }

    private void writeDefaultNamespaceVar(final AnalysedRecord analysedRecord) {
        xmlRootElementPrism.map(XmlRootElementPrism::namespace)
            .filter(StringUtils::isNotBlank)
            .filter(Predicate.not(XML_DEFAULT_STRING::equals))
            .or(
                () -> xmlTypePrism
                        .map(XmlTypePrism::namespace)
                        .filter(StringUtils::isNotBlank)
                        .filter(Predicate.not(XML_DEFAULT_STRING::equals))
            )
            .ifPresentOrElse(
                namespace -> xmlStaticClass.addField(
                    FieldSpec.builder(OPTIONAL_STRING, XML_DEFAULT_NAMESPACE_VAR_NAME, Modifier.FINAL, Modifier.STATIC, Modifier.PRIVATE)
                        .initializer("$T.of($S)", OPTIONAL, namespace)
                        .build()
                ),
                () -> xmlStaticClass.addField(
                    FieldSpec.builder(OPTIONAL_STRING, XML_DEFAULT_NAMESPACE_VAR_NAME, Modifier.FINAL, Modifier.STATIC, Modifier.PRIVATE)
                        .initializer("$T.empty()", OPTIONAL)
                        .build()
                )
            );
        if (xmlRootElementPrism.isPresent()) {
            XmlSchemaPrism.getOptionalOn(analysedRecord.typeElement().getEnclosingElement())
                .map(XmlSchemaPrism::namespace)
                .filter(StringUtils::isNotBlank)
                .filter(Predicate.not(XML_DEFAULT_STRING::equals))
                .ifPresentOrElse(
                    namespace -> xmlStaticClass.addField(
                        FieldSpec.builder(OPTIONAL_STRING, XML_PACKAGE_NAMESPACE_VAR_NAME, Modifier.FINAL, Modifier.STATIC, Modifier.PRIVATE)
                            .initializer("$T.of($S)", OPTIONAL, namespace)
                            .build()
                    ),
                    () -> xmlStaticClass.addField(
                        FieldSpec.builder(OPTIONAL_STRING, XML_PACKAGE_NAMESPACE_VAR_NAME, Modifier.FINAL, Modifier.STATIC, Modifier.PRIVATE)
                            .initializer("$T.empty()", OPTIONAL)
                            .build()
                    )
                );
        }
    }

    private void writeDefaultTagVar(final AnalysedRecord analysedRecord) {
        xmlStaticClass.addField(
            FieldSpec.builder(STRING, XML_DEFAULT_TAG_NAME_VAR_NAME, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", elementName(analysedRecord))
                .build()
        );
    }

    private void writeCreateTag() {
        final var methodBuilder = xmlStaticClass.createMethod("createTag", claimableOperation, OPTIONAL);
        methodBuilder.modifiers.clear();
        methodBuilder.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .returns(STRING)
            .addJavadoc("Determine the tag to write out for the current XML Element")
            .addParameter(
                ParameterSpec.builder(STRING, "incomingTag", Modifier.FINAL)
                    .addJavadoc("The incoming tag that was requested")
                    .build()
            )
            .addStatement(
                """
                    return $T.ofNullable(incomingTag)
                        .filter($T::nonNull)
                        .filter($T.not($T::isBlank))
                        .filter(x -> !$T.XML_DEFAULT_STRING.equals(x))
                        .orElse($L)
                """.trim(),
                OPTIONAL, 
                OBJECTS, 
                PREDICATE,
                STRING,
                ARU_GENERATED,
                XML_DEFAULT_TAG_NAME_VAR_NAME
            );
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
    }

    private void writeCreateNamespace(final AnalysedRecord analysedRecord) {
        if (XmlRootElementPrism.isPresent(analysedRecord.typeElement())) {
            writeCreateNamespaceRoot();
        } else {
            writeCreateNamespaceNonRoot();
        }
    }

    private void writeCreateNamespaceRoot() {
        final var methodBuilder = xmlStaticClass.createMethod("createNamespace", claimableOperation, OPTIONAL);
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        methodBuilder.modifiers.clear();
        methodBuilder.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .returns(OPTIONAL_STRING)
            .addJavadoc("Determine the final namespace of the current XmlRootElement")
            .addParameter(
                REQ_NS_PARAM
            )
            .addParameter(
                CURR_DEF_NS_PARAM
            )
            .addStatement(
                """
                return $T.ofNullable(requestedNamespace)
                    .filter($T::nonNull)
                    .filter($T.not($T::isBlank))
                    .filter(x -> !$T.XML_DEFAULT_STRING.equals(x))
                    .or(() -> $L)
                    .or(
                        () -> $T.ofNullable(currentDefaultNamespace)
                            .filter($T::nonNull)
                            .filter($T.not($T::isBlank))
                            .filter(x -> !$T.XML_DEFAULT_STRING.equals(x))
                    )
                    .or(() -> $L)
                """.trim(),
                OPTIONAL,
                OBJECTS, 
                PREDICATE,
                STRING,
                ARU_GENERATED,
                XML_DEFAULT_NAMESPACE_VAR_NAME,
                OPTIONAL,
                OBJECTS, 
                PREDICATE,
                STRING,
                ARU_GENERATED,
                XML_PACKAGE_NAMESPACE_VAR_NAME
            );
    }

    private void writeCreateNamespaceNonRoot() {
        final var methodBuilder = xmlStaticClass.createMethod("createNamespace", claimableOperation, OPTIONAL);
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        methodBuilder.modifiers.clear();
        methodBuilder.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .returns(OPTIONAL_STRING)
            .addJavadoc("Determine the final namespace of the current XmlType")
            .addParameter(
                REQ_NS_PARAM
            )
            .addParameter(
                CURR_DEF_NS_PARAM
            )
            .addStatement(
                """
                return $T.ofNullable(requestedNamespace)
                    .filter($T::nonNull)
                    .filter($T.not($T::isBlank))
                    .filter(x -> !$T.XML_DEFAULT_STRING.equals(x))
                    .or(() -> $L)
                    .or(
                        () -> $T.ofNullable(currentDefaultNamespace)
                            .filter($T::nonNull)
                            .filter($T.not($T::isBlank))
                            .filter(x -> !$T.XML_DEFAULT_STRING.equals(x))
                    )
                """.trim(),
                OPTIONAL,
                OBJECTS, 
                PREDICATE,
                STRING,
                ARU_GENERATED,
                XML_DEFAULT_NAMESPACE_VAR_NAME,
                OPTIONAL,
                OBJECTS, 
                PREDICATE,
                STRING,
                ARU_GENERATED
            );
    }

    private void writeChangeDefaultNamespace(final MethodSpec.Builder methodBuilder) {
        final ClassName xmlUtilCn = xmlStaticClass.className();
        if (xmlRootElementPrism.isPresent()) {
            methodBuilder.beginControlFlow("if ($T.$L.isPresent()) ", xmlUtilCn, XML_DEFAULT_NAMESPACE_VAR_NAME)
            .addStatement(
                "output.setDefaultNamespace($T.$L.get())", xmlUtilCn, XML_DEFAULT_NAMESPACE_VAR_NAME
            )
            .endControlFlow()
            .addStatement(
                "final @$T String namespaceToPassDown = $T.$L.orElse(currentDefaultNamespace)", NULLABLE, xmlUtilCn, XML_DEFAULT_NAMESPACE_VAR_NAME
            );
            
        } else {
            methodBuilder.addStatement(
                "final $T defNs = $T.$L.filter(ignored -> $T.nonNull(requestedNamespace) && (!requestedNamespace.isBlank()))", OPTIONAL_STRING, xmlUtilCn, XML_DEFAULT_NAMESPACE_VAR_NAME, OBJECTS
            )
            .beginControlFlow("if (defNs.isPresent())")
            .addStatement(
                "output.setDefaultNamespace(defNs.get())"
            )
            .endControlFlow()
            .addStatement(
                "final @$T String namespaceToPassDown = defNs.orElse(currentDefaultNamespace)", NULLABLE
            );
        }
    }

    private void writeSelfToExistingWithTagAndNamespace(final AnalysedRecord analysedRecord) {
        final MethodSpec.Builder methodBuilder = createMethod(analysedRecord)
            .addStatement("final $T tag = $T.$L(requestedTagName)", STRING, xmlStaticClass.className(), "createTag")
            .addStatement("final $T namespace = $T.createNamespace(requestedNamespace, currentDefaultNamespace)", OPTIONAL_STRING, xmlStaticClass.className());
        configureNamespaceContext(analysedRecord, methodBuilder);
        writeChangeDefaultNamespace(methodBuilder);
        methodBuilder.beginControlFlow("if (namespace.isPresent())")
            .addStatement("output.writeStartElement(namespace.get(), tag)")
            .nextControlFlow("else")
            .addStatement("output.writeStartElement(tag)")
            .endControlFlow();
        addWriteOrderComment(methodBuilder);
        // OK, now to write out all the components... attributes first, then elements, then within those in order... booooooo
        final List<String> propOrder = XmlTypePrism.getOptionalOn(analysedRecord.typeElement())
            .map(XmlTypePrism::propOrder)
            .filter(Objects::nonNull)
            .map(props -> props.stream().filter(StringUtils::isNotBlank).toList())
            .orElse(List.of());
    
        final List<AnalysedComponent> writeOrder = new ArrayList<>(components.size());
        manuallyOrderedAttributes(components, propOrder, writeOrder);
        remainingAttributes(components, writeOrder);
        manuallyOrderedElements(components, propOrder, writeOrder);
        remainingElements(components, writeOrder);

        for(final AnalysedComponent component : writeOrder) {
            final String name = component.name();
            if (component instanceof TypeAliasComponent) {
                methodBuilder.addStatement("$T.$L(output, el.$L().value(), namespaceToPassDown)", xmlStaticClass.className(), name, name);
            } else {
                methodBuilder.addStatement("$T.$L(output, el.$L(), namespaceToPassDown)", xmlStaticClass.className(), name, name);
            }
        }
        methodBuilder.addStatement("output.writeEndElement()");

        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
    }

    private MethodSpec.Builder createMethod(final AnalysedRecord analysedRecord) {
        return xmlStaticClass.createMethod(STATIC_WRITE_XML_NAME, claimableOperation)
            .addModifiers(Modifier.STATIC)
            .addJavadoc("Write out the provided instance to the requested {@link $T}, referring to itself with the requested tag name", XML_STREAM_WRITER)
            .addJavadoc("\n<p>\n")
            .addJavadoc("This method will close any tags it opens. It is not expected that it will start or end the document.")
            .addException(XML_STREAM_EXCEPTION)
            .addParameter(
                ParameterSpec.builder(analysedRecord.intendedType(), "el", Modifier.FINAL)
                    .addJavadoc("The item to write to XML")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(XML_STREAM_WRITER, "output", Modifier.FINAL)
                    .addJavadoc("The output to write to")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(STRING.annotated(CommonsConstants.NULLABLE_ANNOTATION), "requestedTagName", Modifier.FINAL)
                    .addJavadoc("The tag name requested for this element. If null, will use the default tag name")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(STRING.annotated(CommonsConstants.NULLABLE_ANNOTATION), "requestedNamespace", Modifier.FINAL)
                    .addJavadoc("The namespace requested for this element. If null, will use the default namespace (NOT the one on the element)")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(STRING.annotated(CommonsConstants.NULLABLE_ANNOTATION), "currentDefaultNamespace", Modifier.FINAL)
                    .addJavadoc("The current default namespace")
                    .build()
            )
            .addStatement("$T.requireNonNull(el, $S)", OBJECTS, "Cannot supply null element to be written to XML")
            .addStatement("$T.requireNonNull(output, $S)", OBJECTS, "Cannot supply null output for XML content");
    }

}
