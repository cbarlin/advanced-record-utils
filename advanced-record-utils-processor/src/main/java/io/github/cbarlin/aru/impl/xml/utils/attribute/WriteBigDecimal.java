package io.github.cbarlin.aru.impl.xml.utils.attribute;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.types.TypeAliasComponent;
import io.github.cbarlin.aru.impl.wiring.XmlPerComponentScope;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;
import jakarta.inject.Singleton;

import java.util.Optional;

import static io.github.cbarlin.aru.impl.Constants.Names.BIG_DECIMAL;
import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;

@Singleton
@XmlPerComponentScope
@RequiresBean(XmlAttributePrism.class)
public final class WriteBigDecimal extends WriteXmlAttribute {
    
    public WriteBigDecimal(final XmlRecordHolder xmlRecordHolder, final XmlAttributePrism xmlAttributePrism, final Optional<TypeAliasComponent> typeAliasComponent) {
        super(xmlRecordHolder, xmlAttributePrism, typeAliasComponent);
    }

    @Override
    TypeName supportedTypeName() {
        return BIG_DECIMAL;
    }

    @Override
    void visitAttributeComponent(AnalysedComponent analysedComponent, XmlAttributePrism prism) {
        final MethodSpec.Builder methodBuilder = createMethod(analysedComponent, supportedTypeName());
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
            namespace -> methodBuilder.addStatement("output.writeAttribute($S, $S, val.toString())", namespace, attributeName),
            () -> methodBuilder.addStatement("output.writeAttribute($S, val.toString())", attributeName)
        );

        if (!required) {
            methodBuilder.endControlFlow();
        }
    }
}
