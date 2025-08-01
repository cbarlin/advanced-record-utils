package io.github.cbarlin.aru.impl.xml.utils.attribute;

import static io.github.cbarlin.aru.impl.Constants.Names.STRING;

import java.util.Optional;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public final class WritePrimitiveByte extends WriteXmlAttribute {

    public WritePrimitiveByte() {
        super();
    }

    @Override
    TypeName supportedTypeName() {
        return TypeName.BYTE;
    }

    @Override
    void visitAttributeComponent(final AnalysedComponent analysedComponent, final XmlAttributePrism prism) {
        final MethodSpec.Builder methodBuilder = createMethod(analysedComponent);
        final String attributeName = attributeName(analysedComponent, prism);
        final Optional<String> namespaceName = namespaceName(prism);
        namespaceName.ifPresentOrElse(
            namespace -> methodBuilder.addStatement("output.writeAttribute($S, $S, $T.valueOf(val))", namespace, attributeName, STRING),
            () -> methodBuilder.addStatement("output.writeAttribute($S, $T.valueOf(val))", attributeName, STRING)
        );
    }
}
