package io.github.cbarlin.aru.impl.xml.utils.attribute;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.DATE_TIME_FORMATTER;
import static io.github.cbarlin.aru.impl.Constants.Names.LOCAL_DATE_TIME;
import static io.github.cbarlin.aru.impl.Constants.Names.ZONE_ID;
import static io.github.cbarlin.aru.impl.Constants.Names.ZONE_OFFSET;

import java.util.Optional;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.types.TypeAliasComponent;
import io.github.cbarlin.aru.impl.wiring.XmlPerComponentScope;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;
import jakarta.inject.Singleton;

@Singleton
@XmlPerComponentScope
@RequiresBean(XmlAttributePrism.class)
public final class WriteLocalDateTime extends WriteXmlAttribute {

    public WriteLocalDateTime(final XmlRecordHolder xmlRecordHolder, final XmlAttributePrism xmlAttributePrism, final Optional<TypeAliasComponent> typeAliasComponent) {
        super(xmlRecordHolder, xmlAttributePrism, typeAliasComponent);
    }

    @Override
    TypeName supportedTypeName() {
        return LOCAL_DATE_TIME;
    }

    @Override
    void visitAttributeComponent(final AnalysedComponent analysedComponent, final XmlAttributePrism prism) {
        final MethodSpec.Builder methodBuilder = createMethod(analysedComponent)
            .addJavadoc("\n<p>\n")
            .addJavadoc("Will convert to UTC by assuming that the system default time zone is the zone of the LocalDateTime");
        final boolean required = Boolean.TRUE.equals(prism.required());
        final String attributeName = attributeName(analysedComponent, prism);
        final Optional<String> namespaceName = namespaceName(prism);

        if (required) {
            final String errMsg = XML_CANNOT_NULL_REQUIRED_ATTRIBUTE.formatted(analysedComponent.name(), attributeName);
            methodBuilder.addStatement("$T.requireNonNull(val, $S)", OBJECTS, errMsg);
        } else {
            methodBuilder.beginControlFlow("if ($T.nonNull(val))", OBJECTS);
        }
        logTrace(methodBuilder, "Converting value to UTC - assuming that LocalDateTime has the System Default time zone");
        namespaceName.ifPresentOrElse(
            namespace -> methodBuilder.addStatement("output.writeAttribute($S, $S, val.atZone($T.systemDefault()).withZoneSameInstant($T.UTC).format($T.ISO_OFFSET_DATE_TIME))", namespace, attributeName, ZONE_ID, ZONE_OFFSET, DATE_TIME_FORMATTER),
            () -> methodBuilder.addStatement("output.writeAttribute($S, val.atZone($T.systemDefault()).withZoneSameInstant($T.UTC).format($T.ISO_OFFSET_DATE_TIME))", attributeName, ZONE_ID, ZONE_OFFSET, DATE_TIME_FORMATTER)
        );

        if (!required) {
            methodBuilder.endControlFlow();
        }
    }
}
