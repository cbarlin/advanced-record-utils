package io.github.cbarlin.aru.impl.xml.utils.attribute;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.TypeAliasComponent;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import java.util.Optional;

public abstract class WriteXmlAttribute extends XmlVisitor {

    protected final XmlAttributePrism xmlAttributePrism;
    protected final Optional<TypeAliasComponent> typeAliasComponent;

    protected WriteXmlAttribute(final XmlRecordHolder xmlRecordHolder, final XmlAttributePrism xmlAttributePrism, final Optional<TypeAliasComponent> typeAliasComponent) {
        super(Claims.XML_WRITE_FIELD, xmlRecordHolder);
        this.xmlAttributePrism = xmlAttributePrism;
        this.typeAliasComponent = typeAliasComponent;
    }

    abstract TypeName supportedTypeName();

    abstract void visitAttributeComponent(final AnalysedComponent analysedComponent, final XmlAttributePrism prism);

    @Override
    protected int innerSpecificity() {
        return 1;
    }

    @Override
    protected final boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final TypeName serialTypeName = typeAliasComponent.map(AnalysedComponent::serialisedTypeName)
                .orElse(analysedComponent.serialisedTypeName());
        if (supportedTypeName().equals(serialTypeName)) {
            visitAttributeComponent(analysedComponent, xmlAttributePrism);
            return true;
        }
        return false;
    }

    protected final MethodSpec.Builder createMethod(final AnalysedComponent analysedComponent) {
        return createMethod(analysedComponent, analysedComponent.serialisedTypeName());
    }
}
