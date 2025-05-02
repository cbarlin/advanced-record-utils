package io.github.cbarlin.aru.impl.xml.utils.attribute;

import static io.github.cbarlin.aru.impl.Constants.Names.STRING;
import static io.github.cbarlin.aru.impl.Constants.Names.STRINGUTILS;
import static io.github.cbarlin.aru.impl.Constants.Names.VALIDATE;

import java.util.Optional;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public class WriteString extends WriteXmlAttribute {

    public WriteString() {
        super();
    }

    @Override
    protected int innerSpecificity() {
        return 2;
    }

    @Override
    TypeName supportedTypeName() {
        return STRING;
    }

    @Override
    void visitAttributeComponent(final AnalysedComponent analysedComponent, final XmlAttributePrism prism) {
        final MethodSpec.Builder methodBuilder = createMethod(analysedComponent);
        final boolean required = Boolean.TRUE.equals(prism.required());
        final String attributeName = attributeName(analysedComponent, prism);
        final Optional<String> namespaceName = namespaceName(prism);

        if (required) {
            final String errMsg = XML_CANNOT_NULL_REQUIRED_ATTRIBUTE.formatted(analysedComponent.name(), attributeName);
            methodBuilder.addStatement("$T.notBlank(val, $S)", VALIDATE, errMsg);
        } else {
            methodBuilder.beginControlFlow("if ($T.isNotBlank(val))", STRINGUTILS);
        }

        namespaceName.ifPresentOrElse(
            namespace -> methodBuilder.addStatement("output.writeAttribute($S, $S, val)", namespace, attributeName),
            () -> methodBuilder.addStatement("output.writeAttribute($S, val)", attributeName)
        );

        if (!required) {
            methodBuilder.endControlFlow();
        }
    }
}
