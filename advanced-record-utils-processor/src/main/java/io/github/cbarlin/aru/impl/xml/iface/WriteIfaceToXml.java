package io.github.cbarlin.aru.impl.xml.iface;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_DEFAULT_NAMESPACE_VAR_NAME;
import static io.github.cbarlin.aru.impl.Constants.Names.NON_NULL;
import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.STRING;
import static io.github.cbarlin.aru.impl.Constants.Names.STRINGUTILS;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_WRITER;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.TypeAliasComponent;
import io.github.cbarlin.aru.impl.wiring.XmlPerRecordScope;
import io.github.cbarlin.aru.impl.xml.ToXmlMethod;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.prism.prison.XmlRootElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlTypePrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import jakarta.inject.Singleton;

@Singleton
@XmlPerRecordScope
public final class WriteIfaceToXml extends ToXmlMethod {

    private static final ParameterizedTypeName OPTIONAL_STRING = ParameterizedTypeName.get(OPTIONAL, STRING);

    public WriteIfaceToXml(final XmlRecordHolder xmlRecordHolder, final Optional<XmlTypePrism> xmlTypePrism, final Optional<XmlRootElementPrism> xmlRootElementPrism) {
        super(Claims.XML_IFACE_TO_XML, xmlRecordHolder, xmlTypePrism, xmlRootElementPrism);
    }

    @Override
    protected int innerSpecificity() {
        return 0;
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
            // Small statement, not worth creating a "WriteToXmlWithCommonsLang"
            if (OptionalClassDetector.doesDependencyExist(STRINGUTILS)) {
                methodBuilder.addStatement(
                    "final $T defNs = $T.$L.filter(ignored -> $T.isBlank(requestedNamespace))", OPTIONAL_STRING, xmlUtilCn, XML_DEFAULT_NAMESPACE_VAR_NAME, STRINGUTILS
                );
            } else {
                methodBuilder.addStatement(
                    "final $T defNs = $T.$L.filter(ignored -> $T.isNull(requestedNamespace) || (requestedNamespace.isBlank()))", OPTIONAL_STRING, xmlUtilCn, XML_DEFAULT_NAMESPACE_VAR_NAME, OBJECTS
                );
            }
            methodBuilder.beginControlFlow("if (defNs.isPresent())")
            .addStatement(
                "output.setDefaultNamespace(defNs.get())"
            )
            .endControlFlow()
            .addStatement(
                "final @$T String namespaceToPassDown = defNs.orElse(currentDefaultNamespace)", NULLABLE
            );
        }
    }

    @Override
    protected void visitEndOfClassImpl() {
        final MethodSpec.Builder methodBuilder = createMethod()
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
        final List<String> propOrder = xmlTypePrism.map(XmlTypePrism::propOrder)
            .filter(Objects::nonNull)
            .map(props -> props.stream().filter(StringUtils::isNotBlank).toList())
            .orElse(List.of());
    
        final List<AnalysedComponent> writeOrder = new ArrayList<>(components.size());
        manuallyOrderedAttributes(components, propOrder, writeOrder);
        remainingAttributes(components, writeOrder);
        manuallyOrderedElements(components, propOrder, writeOrder);
        remainingElements(components, writeOrder);

        for(AnalysedComponent component : writeOrder) {
            final String name = component.name();
            if (component instanceof TypeAliasComponent) {
                methodBuilder.addStatement("$T.$L(output, this.$L().value(), namespaceToPassDown)", xmlStaticClass.className(), name, name);
            } else {
                methodBuilder.addStatement("$T.$L(output, this.$L(), namespaceToPassDown)", xmlStaticClass.className(), name, name);
            }
        }
        methodBuilder.addStatement("output.writeEndElement()");
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
    }

    private MethodSpec.Builder createMethod() {
        return xmlInterface.createMethod(xmlOptionsPrism.continueAddingToXmlMethodName(), claimableOperation)
            .addModifiers(Modifier.DEFAULT)
            .addJavadoc("Write out the current instance to the requested {@link $T}, referring to itself with the reqested tag name", XML_STREAM_WRITER)
            .addJavadoc("\n<p>\n")
            .addJavadoc("This method will close any tags it opens. It is not expected that it will start or end the document.")
            .addException(XML_STREAM_EXCEPTION)
            .addParameter(
                ParameterSpec.builder(XML_STREAM_WRITER, "output", Modifier.FINAL)
                    .addJavadoc("The output to write to")
                    .addAnnotation(NON_NULL)
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(STRING, "requestedTagName", Modifier.FINAL)
                    .addJavadoc("The tag name requested for this element. If null, will use the default tag name")
                    .addAnnotation(NULLABLE)
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(STRING, "requestedNamespace", Modifier.FINAL)
                    .addJavadoc("The namespace requested for this element. If null, will use the default namespace (NOT the one on the element)")
                    .addAnnotation(NULLABLE)
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(STRING, "currentDefaultNamespace", Modifier.FINAL)
                    .addJavadoc("The current default namespace")
                    .build()
            )
            .addStatement("$T.requireNonNull(output, $S)", OBJECTS, "Cannot supply null output for XML content");
    }
    
}
