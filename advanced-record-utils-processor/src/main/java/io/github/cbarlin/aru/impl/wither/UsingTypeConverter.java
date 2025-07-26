package io.github.cbarlin.aru.impl.wither;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.AnalysedTypeConverter;
import io.github.cbarlin.aru.core.types.components.TypeConverterComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.WitherPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

import static io.github.cbarlin.aru.impl.Constants.Names.NON_NULL;

@Singleton
@WitherPerComponentScope
@RequiresBean({TypeConverterComponent.class})
public final class UsingTypeConverter extends WitherVisitor {

    private final TypeConverterComponent component;

    public UsingTypeConverter(final WitherInterface witherInterface, final AnalysedRecord analysedRecord, final TypeConverterComponent component) {
        super(Claims.WITHER_USE_TYPE_CONVERTER, witherInterface, analysedRecord);
        this.component = component;
    }

    @Override
    protected int witherSpecificity() {
        return 0;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        final String withMethodName = witherOptionsPrism.withMethodPrefix() + capitalise(analysedComponent.name()) + witherOptionsPrism.withMethodSuffix();
        
        for (final AnalysedTypeConverter converter : component.analysedTypeConverters()) {
            final MethodSpec.Builder methodBuilder = witherInterface.createMethod(withMethodName, claimableOperation, converter.executableElement())
                .addAnnotation(NON_NULL)
                .returns(analysedComponent.parentRecord().intendedType())
                .addModifiers(Modifier.DEFAULT)
                .addJavadoc("Return a new instance with a different {@code $L} field", analysedComponent.name());
                
            AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
            final List<String> format = new ArrayList<>();
            final List<Object> args = new ArrayList<>();
            args.add(withMethodName);
            args.add(converter.enclosedClass());
            args.add(converter.methodName());
            for (final ParameterSpec parameter : converter.parameterSpecs()) {
                methodBuilder.addParameter(parameter);
                args.add(parameter.name);
                format.add("$L");
            }
            methodBuilder.addStatement("return this.$L($T.$L(" + StringUtils.join(format, ", ") + "))", args.toArray());
        }        

        return true;
    }
    
}
