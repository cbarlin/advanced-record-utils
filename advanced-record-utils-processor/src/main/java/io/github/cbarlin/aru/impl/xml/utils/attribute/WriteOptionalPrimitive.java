package io.github.cbarlin.aru.impl.xml.utils.attribute;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.AnalysedOptionalPrimitiveComponent;
import io.github.cbarlin.aru.impl.wiring.XmlPerComponentScope;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import jakarta.inject.Singleton;

import java.util.Optional;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.ILLEGAL_ARGUMENT_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.Names.STRING;

@Singleton
@XmlPerComponentScope
@RequiresBean({XmlAttributePrism.class, AnalysedOptionalPrimitiveComponent.class})
public final class WriteOptionalPrimitive extends XmlVisitor {

    private final XmlAttributePrism prism;
    private final AnalysedOptionalPrimitiveComponent component;

    public WriteOptionalPrimitive(final XmlRecordHolder xmlRecordHolder, final XmlAttributePrism xmlAttributePrism, final AnalysedOptionalPrimitiveComponent component) {
        super(Claims.XML_WRITE_FIELD, xmlRecordHolder);
        this.prism = xmlAttributePrism;
        this.component = component;
    }

    @Override
    protected int innerSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final MethodSpec.Builder methodBuilder = createMethod(analysedComponent, component.serialisedTypeName());
        final String attributeName = attributeName(analysedComponent, prism);
        final Optional<String> namespaceName = namespaceName(prism);

        final boolean required = Boolean.TRUE.equals(prism.required());
        methodBuilder.beginControlFlow("if ($T.nonNull(val) && val.isPresent())", OBJECTS);
        

        component.withinUnwrapped(
            varName -> namespaceName.ifPresentOrElse(
                namespace -> methodBuilder.addStatement("output.writeAttribute($S, $S, $T.valueOf($L))", namespace, attributeName, STRING, varName),
                () -> methodBuilder.addStatement("output.writeAttribute($S, $T.valueOf($L))", attributeName, STRING, varName)
            ),
            methodBuilder,
            "val"
        );

        if (required) {
            final String errMsg = XML_CANNOT_NULL_REQUIRED_ATTRIBUTE.formatted(analysedComponent.name(), attributeName);
            methodBuilder.nextControlFlow("else")
                .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, errMsg);
        }
        
        methodBuilder.endControlFlow();

        return true;
    }
}
