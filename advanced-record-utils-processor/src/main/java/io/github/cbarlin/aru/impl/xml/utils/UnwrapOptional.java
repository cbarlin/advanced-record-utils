package io.github.cbarlin.aru.impl.xml.utils;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedOptionalComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.XmlPerComponentScope;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import jakarta.inject.Singleton;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;
import static io.github.cbarlin.aru.impl.Constants.Names.STRING;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_WRITER;

@Singleton
@XmlPerComponentScope
@RequiresBean({AnalysedOptionalComponent.class})
public final class UnwrapOptional extends XmlVisitor {

    private final AnalysedOptionalComponent component;

    public UnwrapOptional(final XmlRecordHolder xmlRecordHolder, final AnalysedOptionalComponent component) {
        super(Claims.XML_UNWRAP_OPTIONAL, xmlRecordHolder);
        this.component = component;
    }

    @Override
    protected int innerSpecificity() {
        return 3;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final MethodSpec.Builder methodBuilder = xmlStaticClass.createMethod(component.name(), claimableOperation, OPTIONAL);
        methodBuilder.modifiers.clear();
        methodBuilder.addParameter(
                ParameterSpec.builder(XML_STREAM_WRITER, "output", Modifier.FINAL)
                    .addJavadoc("The output to write to")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(component.typeNameNullable(), "val", Modifier.FINAL)
                    .addJavadoc("The item to write")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(STRING.annotated(CommonsConstants.NULLABLE_ANNOTATION), "currentDefaultNamespace", Modifier.FINAL)
                    .addJavadoc("The current default namespace")
                    .build()
            )
            .addException(XML_STREAM_EXCEPTION)
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
            .addJavadoc("Add the {@code $L} field to the XML output", component.name())
            .addStatement("$T.$L(output, $T.nonNull(val) ? val.orElse(null) : null, currentDefaultNamespace)", xmlStaticClass.className(), component.name(), OBJECTS);
        
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        return true;
    }
}
