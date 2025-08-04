package io.github.cbarlin.aru.impl.xml.utils.attribute;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.XmlPerComponentScope;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import jakarta.inject.Singleton;

import java.util.Optional;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.CHAR_SEQUENCE;
import static io.github.cbarlin.aru.impl.Constants.Names.ILLEGAL_ARGUMENT_EXCEPTION;

@Singleton
@XmlPerComponentScope
@RequiresBean(XmlAttributePrism.class)
public final class WriteCharSequence extends XmlVisitor {

    private final XmlAttributePrism xmlAttributePrism;
    
    public WriteCharSequence(final XmlRecordHolder xmlRecordHolder, final XmlAttributePrism xmlAttributePrism) {
        super(Claims.XML_WRITE_FIELD, xmlRecordHolder);
        this.xmlAttributePrism = xmlAttributePrism;
    }

    @Override
    protected int innerSpecificity() {
        return 1;
    }
    
    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        final boolean isApplicable = OptionalClassDetector.checkSameOrSubType(analysedComponent.serialisedTypeName(), CHAR_SEQUENCE);
        if (isApplicable) {
            visitAttributeComponent(analysedComponent, xmlAttributePrism);
            return true;
        }
        return false;
    }

    void visitAttributeComponent(final AnalysedComponent analysedComponent, final XmlAttributePrism prism) {
        final MethodSpec.Builder methodBuilder = createMethod(analysedComponent, CHAR_SEQUENCE);
        final boolean required = Boolean.TRUE.equals(prism.required());
        final String attributeName = attributeName(analysedComponent, prism);
        final Optional<String> namespaceName = namespaceName(prism);

        methodBuilder.beginControlFlow("if ($T.nonNull(val) && $T.nonNull(val.toString()) && (!val.toString().isBlank()))", OBJECTS, OBJECTS);
        namespaceName.ifPresentOrElse(
            namespace -> methodBuilder.addStatement("output.writeAttribute($S, $S, val.toString())", namespace, attributeName),
            () -> methodBuilder.addStatement("output.writeAttribute($S, val.toString())", attributeName)
        );

        if(required) {
            final String errMsg = XML_CANNOT_NULL_REQUIRED_ATTRIBUTE.formatted(analysedComponent.name(), attributeName);
            methodBuilder.nextControlFlow("else")
                .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, errMsg)
                .endControlFlow();
        } else {
            methodBuilder.endControlFlow();
        }
    }
}
