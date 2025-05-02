package io.github.cbarlin.aru.impl.xml.utils.attribute;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;

import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

public abstract class WriteXmlAttribute extends XmlVisitor {

    protected WriteXmlAttribute() {
        super(Claims.XML_WRITE_FIELD);
    }

    abstract TypeName supportedTypeName();

    abstract void visitAttributeComponent(final AnalysedComponent analysedComponent, final XmlAttributePrism prism);

    @Override
    protected int innerSpecificity() {
        return 1;
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected final boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        return Boolean.TRUE.equals(
            xmlAttributePrism(analysedComponent)
                .map(prism -> {
                    if (supportedTypeName().equals(analysedComponent.typeName())) {
                        visitAttributeComponent(analysedComponent, prism);
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                })
                .orElse(Boolean.FALSE)
        );
    }

    protected final MethodSpec.Builder createMethod(final AnalysedComponent analysedComponent) {
        return createMethod(analysedComponent, analysedComponent.typeName());
    }
}
