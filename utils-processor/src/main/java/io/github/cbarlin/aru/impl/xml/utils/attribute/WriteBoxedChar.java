package io.github.cbarlin.aru.impl.xml.utils.attribute;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.STRING;

import java.util.Optional;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public class WriteBoxedChar extends WriteXmlAttribute {

    private static final TypeName TN = TypeName.CHAR.box();

    public WriteBoxedChar() {
        super();
    }

    @Override
    TypeName supportedTypeName() {
        return TN;
    }

    @Override
    void visitAttributeComponent(final AnalysedComponent analysedComponent, final XmlAttributePrism prism) {
        final MethodSpec.Builder methodBuilder = createMethod(analysedComponent);
        final boolean required = Boolean.TRUE.equals(prism.required());
        final String attributeName = attributeName(analysedComponent, prism);
        final Optional<String> namespaceName = namespaceName(analysedComponent, prism);

        if (required) {
            final String errMsg = XML_CANNOT_NULL_REQUIRED_ATTRIBUTE.formatted(analysedComponent.name(), attributeName);
            methodBuilder.addStatement("$T.requireNonNull(val, $S)", OBJECTS, errMsg);
        } else {
            methodBuilder.beginControlFlow("if ($T.nonNull(val))", OBJECTS);
        }

        namespaceName.ifPresentOrElse(
            namespace -> methodBuilder.addStatement("output.writeAttribute($S, $S, $T.valueOf(val.charValue()))", namespace, attributeName, STRING),
            () -> methodBuilder.addStatement("output.writeAttribute($S, $T.valueOf(val.charValue()))", attributeName, STRING)
        );

        if (!required) {
            methodBuilder.endControlFlow();
        }
    }
}
