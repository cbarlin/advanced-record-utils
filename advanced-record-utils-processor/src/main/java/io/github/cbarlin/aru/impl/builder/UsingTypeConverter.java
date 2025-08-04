package io.github.cbarlin.aru.impl.builder;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.AnalysedTypeConverter;
import io.github.cbarlin.aru.core.types.components.TypeConverterComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
@BuilderPerComponentScope
@RequiresBean({TypeConverterComponent.class})
public final class UsingTypeConverter extends RecordVisitor {

    private final ToBeBuilt builder;
    private final TypeConverterComponent component;

    public UsingTypeConverter(final BuilderClass builderClass, final AnalysedRecord analysedRecord, final TypeConverterComponent component) {
        super(Claims.BUILDER_USE_TYPE_CONVERTER, analysedRecord);
        this.builder = builderClass.delegate();
        this.component = component;
    }

    @Override
    public int specificity() {
        return 1;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        for (final AnalysedTypeConverter converter : component.analysedTypeConverters()) {
            final MethodSpec.Builder methodBuilder = builder.createMethod(analysedComponent.name(), claimableOperation, converter)
                .addJavadoc("Updates the value of {@code $L}", analysedComponent.name())
                .returns(builder.className());
            AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
            
            final List<String> format = new ArrayList<>();
            final List<Object> args = new ArrayList<>();
            args.add(analysedComponent.name());
            args.add(converter.enclosedClass());
            args.add(converter.methodName());
            for (final ParameterSpec parameter : converter.parameterSpecs()) {
                methodBuilder.addParameter(parameter);
                args.add(parameter.name);
                format.add("$L");
            }
            methodBuilder.addStatement("return this.$L($T.$L(" + StringUtils.join(format, ", ") + "))", args.toArray());
            analysedRecord.addTypeConverter(converter);
        }
        return true;
    }
    
}
