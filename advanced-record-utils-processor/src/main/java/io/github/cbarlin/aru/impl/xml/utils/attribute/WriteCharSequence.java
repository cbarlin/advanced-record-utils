package io.github.cbarlin.aru.impl.xml.utils.attribute;

import static io.github.cbarlin.aru.impl.Constants.Names.CHAR_SEQUENCE;
import static io.github.cbarlin.aru.impl.Constants.Names.STRINGUTILS;
import static io.github.cbarlin.aru.impl.Constants.Names.VALIDATE;

import java.util.Optional;

import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public class WriteCharSequence extends XmlVisitor {

    public WriteCharSequence() {
        super(Claims.XML_WRITE_FIELD);
    }

    @Override
    protected int innerSpecificity() {
        return 1;
    }
    
    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        return Boolean.TRUE.equals(
            xmlAttributePrism(analysedComponent)
                .map(prism -> {
                    final boolean isApplicable = OptionalClassDetector.checkSameOrSubType(analysedComponent.serialisedTypeName(), CHAR_SEQUENCE);
                    if (isApplicable) {
                        visitAttributeComponent(analysedComponent, prism);
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                })
                .orElse(Boolean.FALSE)
        );
    }

    void visitAttributeComponent(final AnalysedComponent analysedComponent, final XmlAttributePrism prism) {
        final MethodSpec.Builder methodBuilder = createMethod(analysedComponent, CHAR_SEQUENCE);
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
            namespace -> methodBuilder.addStatement("output.writeAttribute($S, $S, val.toString())", namespace, attributeName),
            () -> methodBuilder.addStatement("output.writeAttribute($S, val.toString())", attributeName)
        );

        if (!required) {
            methodBuilder.endControlFlow();
        }
    }

    @Override
    protected boolean innerIsApplicable(AnalysedRecord analysedRecord) {
        return true;
    }
}
