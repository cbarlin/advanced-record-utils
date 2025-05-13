package io.github.cbarlin.aru.impl.xml.utils.attribute;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.ILLEGAL_ARGUMENT_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.Names.STRING;

import java.util.Optional;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.AnalysedOptionalPrimitiveComponent;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public class WriteOptionalPrimitive extends XmlVisitor {

    public WriteOptionalPrimitive() {
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
        final Optional<XmlAttributePrism> attribute = xmlAttributePrism(analysedComponent);
        if (attribute.isPresent() && (analysedComponent instanceof final AnalysedOptionalPrimitiveComponent component)) {
            final XmlAttributePrism prism = attribute.get();
            final MethodSpec.Builder methodBuilder = createMethod(analysedComponent, analysedComponent.serialisedTypeName());
            final String attributeName = attributeName(analysedComponent, prism);
            final Optional<String> namespaceName = namespaceName(prism);

            final boolean required = Boolean.TRUE.equals(prism.required());
            methodBuilder.beginControlFlow("if ($T.nonNull(val) && val.isPresent)", OBJECTS);
            

            component.withinUnwrapped(
                varName -> {
                    final String methodName = component.getterMethod();
                    namespaceName.ifPresentOrElse(
                        namespace -> methodBuilder.addStatement("output.writeAttribute($S, $S, $T.valueOf($L.$L()))", namespace, attributeName, STRING, varName, methodName),
                        () -> methodBuilder.addStatement("output.writeAttribute($S, $T.valueOf($L.$L()))", attributeName, STRING, varName, methodName)
                    );
                },
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
        return false;
    }
}
