package io.github.cbarlin.aru.impl.xml.utils.attribute;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.DATE_TIME_FORMATTER;
import static io.github.cbarlin.aru.impl.Constants.Names.ZONED_DATE_TIME;
import static io.github.cbarlin.aru.impl.Constants.Names.ZONE_OFFSET;

import java.util.Optional;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public final class WriteZonedDateTime extends WriteXmlAttribute {

    public WriteZonedDateTime() {
        super();
    }

    @Override
    TypeName supportedTypeName() {
        return ZONED_DATE_TIME;
    }

    @Override
    void visitAttributeComponent(final AnalysedComponent analysedComponent, final XmlAttributePrism prism) {
        final MethodSpec.Builder methodBuilder = createMethod(analysedComponent);
        final boolean required = Boolean.TRUE.equals(prism.required());
        final String attributeName = attributeName(analysedComponent, prism);
        final Optional<String> namespaceName = namespaceName(prism);

        if (required) {
            final String errMsg = XML_CANNOT_NULL_REQUIRED_ATTRIBUTE.formatted(analysedComponent.name(), attributeName);
            methodBuilder.addStatement("$T.requireNonNull(val, $S)", OBJECTS, errMsg);
        } else {
            methodBuilder.beginControlFlow("if ($T.nonNull(val))", OBJECTS);
        }

        namespaceName.ifPresentOrElse(
            namespace -> methodBuilder.addStatement("output.writeAttribute($S, $S, val.toOffsetDateTime().atZoneSameInstant($T.UTC).format($T.ISO_OFFSET_DATE_TIME))", namespace, attributeName, ZONE_OFFSET, DATE_TIME_FORMATTER),
            () -> methodBuilder.addStatement("output.writeAttribute($S, val.toOffsetDateTime().atZoneSameInstant($T.UTC).format($T.ISO_OFFSET_DATE_TIME))", attributeName, ZONE_OFFSET, DATE_TIME_FORMATTER)
        );

        if (!required) {
            methodBuilder.endControlFlow();
        }
    }
}
