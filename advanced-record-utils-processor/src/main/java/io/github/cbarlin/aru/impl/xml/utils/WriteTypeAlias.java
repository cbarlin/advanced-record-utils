package io.github.cbarlin.aru.impl.xml.utils;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.Constants;
import io.github.cbarlin.aru.impl.types.TypeAliasComponent;
import io.github.cbarlin.aru.impl.wiring.XmlPerComponentScope;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import jakarta.inject.Singleton;

import javax.lang.model.element.Modifier;
import java.util.Optional;

import static io.github.cbarlin.aru.impl.Constants.Names.STRING;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_STREAM_WRITER;

@Singleton
@XmlPerComponentScope
@RequiresBean({TypeAliasComponent.class})
public final class WriteTypeAlias extends XmlVisitor {

    private final TypeAliasComponent typeAliasComponent;

    public WriteTypeAlias(final XmlRecordHolder xmlHolder, final TypeAliasComponent typeAliasComponent) {
        super(Constants.Claims.XML_UNWRAP_TYPE_COMPONENT, xmlHolder);
        this.typeAliasComponent = typeAliasComponent;
    }

    @Override
    protected int innerSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final Optional<ClassName> className = typeAliasComponent.className();
        if (className.isEmpty()) {
            return false;
        }
        final MethodSpec.Builder methodBuilder = xmlStaticClass.createMethod(analysedComponent.name(), claimableOperation, className.get());
        methodBuilder.modifiers.clear();
        methodBuilder.addParameter(
                    ParameterSpec.builder(XML_STREAM_WRITER, "output", Modifier.FINAL)
                            .addJavadoc("The output to write to")
                            .build()
            )
            .addParameter(
                    ParameterSpec.builder(analysedComponent.typeNameNullable(), "val", Modifier.FINAL)
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
            .addJavadoc("Add the {@code $L} field to the XML output", analysedComponent.name())
            .addStatement(
                    "$T.$L(output, val.$L(), currentDefaultNamespace)",
                    xmlStaticClass.className(),
                    analysedComponent.name(),
                    typeAliasComponent.valueMethodName()
            );
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        return true;
    }
}
