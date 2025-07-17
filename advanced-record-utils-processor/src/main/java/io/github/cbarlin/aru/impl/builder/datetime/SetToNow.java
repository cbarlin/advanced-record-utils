package io.github.cbarlin.aru.impl.builder.datetime;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;

public abstract class SetToNow extends RecordVisitor {

    private final ClassName dateTimeClassName;

    protected SetToNow(final ClassName dateTimeClassName) {
        super(Claims.BUILDER_SET_TIME_TO_NOW);
        this.dateTimeClassName = dateTimeClassName;
    }

    @Override
    public int specificity() {
        return 3;
    }

    @Override
    public boolean isApplicable(final AnalysedRecord analysedRecord) {
        return Boolean.TRUE.equals(analysedRecord.settings().prism().builderOptions().setTimeNowMethods());
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        if (analysedComponent.isIntendedConstructorParam() && dateTimeClassName.equals(analysedComponent.typeName())) {
            final MethodSpec.Builder methodBuilder = analysedComponent.builderArtifact()
                .createMethod( 
                    analysedComponent.settings().prism().builderOptions().setTimeNowMethodPrefix() + 
                        analysedComponent.nameFirstLetterCaps() +
                        analysedComponent.settings().prism().builderOptions().setTimeNowMethodSuffix(), 
                    claimableOperation
                );
            AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
            
            methodBuilder.returns(analysedComponent.builderArtifact().className())
                .addJavadoc("Sets the value of the {@code $L} to the current time", analysedComponent.name())
                .addAnnotation(CommonsConstants.Names.NON_NULL)
                .addStatement("return this.$L($T.now())", analysedComponent.name(), dateTimeClassName);
        }
        return false;
    }

}
