package io.github.cbarlin.aru.impl.wither;

import static io.github.cbarlin.aru.impl.Constants.Names.NON_NULL;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public final class BackToBuilder extends WitherVisitor {

    public BackToBuilder() {
        super(Claims.WITHER_TO_BUILDER);
    }

    @Override
    protected boolean isWitherApplicable(AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int witherSpecificity() {
        return 0;
    }

    @Override
    protected boolean visitStartOfClassImpl(final AnalysedRecord analysedRecord) {
        final ClassName builderClassName = analysedRecord.builderArtifact().className();
        final String backToBuilderMethodName = witherOptionsPrism.convertToBuilder();
        final MethodSpec.Builder backBuilder = witherInterface.createMethod(backToBuilderMethodName, claimableOperation)
            .addModifiers(Modifier.DEFAULT)
            .returns(builderClassName)
            .addJavadoc("Creates a builder with the current fields")
            .addAnnotation(NON_NULL);
        AnnotationSupplier.addGeneratedAnnotation(backBuilder, this);
        final List<Object> methodArgs = new ArrayList<>();
        final StringBuilder methodFormat = new StringBuilder().append("return $T.$L()\n");
        methodArgs.add(builderClassName);
        methodArgs.add(builderOptionsPrism.emptyCreationName());
        for (final VariableElement parameter : analysedRecord.intendedConstructor().getParameters()) {
            methodFormat.append(".$L(this.$L())\n");
            methodArgs.add(parameter.getSimpleName());
            methodArgs.add(parameter.getSimpleName());
        }
        
        backBuilder.addStatement(methodFormat.toString().trim(), methodArgs.toArray());

        return true;
    }

}
