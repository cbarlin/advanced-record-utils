package io.github.cbarlin.aru.impl.xml.utils.elements.noncollections;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedOptionalComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.TypeAliasComponent;
import io.github.cbarlin.aru.impl.wiring.XmlPerComponentScope;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;
import jakarta.inject.Singleton;

import java.util.Optional;

import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.STRING;

@Singleton
@XmlPerComponentScope
@RequiresBean({XmlElementPrism.class})
public final class WriteBoxedBoolean extends NonCollectionXmlVisitor {

    private final XmlElementPrism prism;

    public WriteBoxedBoolean(
            final XmlRecordHolder xmlRecordHolder,
            final XmlElementPrism prism,
            final Optional<AnalysedOptionalComponent> analysedOptionalComponent,
            final Optional<TypeAliasComponent> typeAliasComponent
    ) {
        super(Claims.XML_WRITE_FIELD, xmlRecordHolder, analysedOptionalComponent, typeAliasComponent);
        this.prism = prism;
    }

    @Override
    protected int innerSpecificity() {
        return 1;
    }

    @Override
    protected boolean writeElementMethod(final AnalysedComponent analysedComponent) {
        if(!TypeName.BOOLEAN.box().equals(analysedComponent.serialisedTypeName())) {
            return false;
        }
        final String elementName = findElementName(analysedComponent, prism);
        final boolean required = Boolean.TRUE.equals(prism.required());
        final Optional<String> defaultValue = defaultValue(prism);
        final Optional<String> namespaceName = namespaceName(prism);
        final MethodSpec.Builder methodBuilder = createMethod(analysedComponent, analysedComponent.serialisedTypeName());
        if (defaultValue.isPresent()) {
            writeWithDefaultDefined(analysedComponent, elementName, defaultValue.get(), namespaceName, methodBuilder);
        } else {
            writeWithoutDefaultDefined(analysedComponent, elementName, required, namespaceName, methodBuilder);
        }

        return true;
    }

    private void writeWithoutDefaultDefined(
            final AnalysedComponent analysedComponent, 
            final String elementName, 
            final boolean required,
            final Optional<String> namespaceName, 
            final MethodSpec.Builder methodBuilder
    ) {
        if (required) {
            final String errMsg = XML_CANNOT_NULL_REQUIRED_ELEMENT.formatted(analysedComponent.name(), elementName);
            methodBuilder.addStatement("$T.requireNonNull(val, $S)", OBJECTS, errMsg);
        } else {
            methodBuilder.beginControlFlow("if($T.nonNull(val))", OBJECTS);
        }

        namespaceName.ifPresentOrElse(
            namespace -> methodBuilder.addStatement("output.writeStartElement($S, $S)", namespace, elementName),
            () -> methodBuilder.addStatement("output.writeStartElement($S)", elementName)
        );
        methodBuilder.addStatement("output.writeCharacters($T.valueOf($T.TRUE.equals(val)))", STRING, TypeName.BOOLEAN.box())
            .addStatement("output.writeEndElement()");
      
        if (!required) {
            methodBuilder.endControlFlow();
        }
    }

    private void writeWithDefaultDefined(
            final AnalysedComponent analysedComponent, 
            final String elementName,
            final String writeAsDefaultValue, 
            final Optional<String> namespaceName,
            final MethodSpec.Builder methodBuilder
    ) {
        namespaceName.ifPresentOrElse(
            namespace -> methodBuilder.addStatement("output.writeStartElement($S, $S)", namespace, elementName),
            () -> methodBuilder.addStatement("output.writeStartElement($S)", elementName)
        );
        methodBuilder.beginControlFlow("if($T.nonNull(val))", OBJECTS)
            .addStatement("output.writeCharacters($T.valueOf($T.TRUE.equals(val)))", STRING, TypeName.BOOLEAN.box())
            .nextControlFlow("else");
        logTrace(methodBuilder, "Supplied value for %s (element name %s) was null/blank, writing default of %s".formatted(analysedComponent.name(), elementName, writeAsDefaultValue));
        methodBuilder.addStatement("output.writeCharacters($S)", writeAsDefaultValue)
            .endControlFlow()
            .addStatement("output.writeEndElement()");
    }
}
