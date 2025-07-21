package io.github.cbarlin.aru.impl.xml.utils.elements.noncollections;

import static io.github.cbarlin.aru.impl.Constants.Names.STRING;

import java.util.Optional;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public class WritePrimitiveBoolean extends XmlVisitor {

    public WritePrimitiveBoolean() {
        super(Claims.XML_WRITE_FIELD);
    }

    @Override
    protected int innerSpecificity() {
        return 1;
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final Optional<XmlElementPrism> optPrism = xmlElementPrism(analysedComponent);
        if (optPrism.isPresent() && (!analysedComponent.isLoopable()) && TypeName.BOOLEAN.equals(analysedComponent.unNestedPrimaryTypeName())) {
            final XmlElementPrism prism = optPrism.get();
            final String elementName = elementName(analysedComponent, prism);
            final Optional<String> namespaceName = namespaceName(prism);
            final MethodSpec.Builder methodBuilder = createMethod(analysedComponent, analysedComponent.unNestedPrimaryTypeName());

            defaultValue(prism).ifPresent(ignored -> APContext.messager().printNote("Primitives will never use their defaults - ignoring requested default", analysedComponent.element()));

            namespaceName.ifPresentOrElse(
                namespace -> methodBuilder.addStatement("output.writeStartElement($S, $S)", namespace, elementName),
                () -> methodBuilder.addStatement("output.writeStartElement($S)", elementName)
            );
            methodBuilder.addStatement("output.writeCharacters($T.valueOf(val))", STRING)
                .addStatement("output.writeEndElement()");
            return true;
        }
        return false;
    }
}
