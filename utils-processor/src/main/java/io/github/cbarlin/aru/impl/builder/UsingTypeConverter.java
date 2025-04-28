package io.github.cbarlin.aru.impl.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.AnalysedTypeConverter;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public class UsingTypeConverter extends RecordVisitor {

    private ToBeBuilt builder;

    public UsingTypeConverter() {
        super(Claims.BUILDER_USE_TYPE_CONVERTER);
    }

    @Override
    public int specificity() {
        return 1;
    }

    @Override
    public boolean isApplicable(final AnalysedRecord analysedRecord) {
        builder = analysedRecord.builderArtifact();
        return true;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        for (final AnalysedTypeConverter converter : analysedComponent.converters()) {
            final MethodSpec.Builder methodBuilder = builder.createMethod(analysedComponent.name(), claimableOperation, converter.executableElement())
                .addJavadoc("Updates the value of {@code $L}", analysedComponent.name())
                .returns(builder.className());
            AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
            
            final List<String> format = new ArrayList<>();
            final List<Object> args = new ArrayList<>();
            args.add(analysedComponent.name());
            args.add(converter.referenceClassName());
            args.add(converter.methodName());
            for (final ParameterSpec parameter : converter.parameters()) {
                methodBuilder.addParameter(parameter);
                args.add(parameter.name);
                format.add("$L");
            }
            methodBuilder.addStatement("return this.$L($T.$L(" + StringUtils.join(format, ", ") + "))", args.toArray());
        }
        return true;
    }
    
}
