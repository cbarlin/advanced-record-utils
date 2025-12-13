package io.github.cbarlin.aru.impl.builder.datetime;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;

public abstract sealed class SetToNow extends RecordVisitor
permits AddSetLocalDateTimeToNow,
        AddSetOffsetDateTimeToNow,
        AddSetZonedDateTimeToNow
{

    private final BuilderClass builder;

    protected SetToNow(final AnalysedRecord analysedRecord, final ClassName dateTimeClassName, final BuilderClass builderClass) {
        super(Claims.BUILDER_SET_TIME_TO_NOW, analysedRecord);
        this.dateTimeClassName = dateTimeClassName;
        this.builder = builderClass;
    }

    private final ClassName dateTimeClassName;

    @Override
    public int specificity() {
        return 3;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        if (dateTimeClassName.equals(analysedComponent.typeName())) {
            final MethodSpec.Builder methodBuilder = builder
                .createMethod( 
                    settings.prism().builderOptions().setTimeNowMethodPrefix() + 
                        analysedComponent.nameFirstLetterCaps() +
                        settings.prism().builderOptions().setTimeNowMethodSuffix(), 
                    claimableOperation
                );
            AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
            
            methodBuilder.returns(builder.className())
                .addJavadoc("Sets the value of the {@code $L} to the current time", analysedComponent.name())
                .addStatement("return this.$L($T.now())", analysedComponent.name(), dateTimeClassName);
        }
        return false;
    }

}
