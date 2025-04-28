package io.github.cbarlin.aru.core.impl.visitors.builder;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;
import io.github.cbarlin.aru.core.CommonsConstants.Names;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.ClassName;

@ServiceProvider
public class AddEmptyConstruction extends RecordVisitor {

    public AddEmptyConstruction() {
        super(Claims.CORE_BUILDER_FROM_NOTHING);
    }

    @Override
    public int specificity() {
        return 0;
    }

    @Override
    public boolean isApplicable(AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected boolean visitStartOfClassImpl(AnalysedRecord analysedRecord) {
        final String methodName = analysedRecord.settings().prism().builderOptions().emptyCreationName();
        final ClassName builderClassName = analysedRecord.builderArtifact().className();
        createOnBuilder(analysedRecord, methodName, builderClassName);
        createOnUtils(analysedRecord, methodName, builderClassName);
        return true;
    }

    private void createOnBuilder(final AnalysedRecord analysedRecord, final String methodName, final ClassName builderClassName) {
        final var builder = analysedRecord.builderArtifact().createMethod(methodName, claimableOperation);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .addAnnotation(Names.NON_NULL)
            .addJavadoc("Create a blank builder of {@link $T}", analysedRecord.intendedType())
            .addStatement("return new $T()", builderClassName)
            .returns(builderClassName);
        AnnotationSupplier.addGeneratedAnnotation(builder, this);   
    }

    private void createOnUtils(final AnalysedRecord analysedRecord, final String methodName, final ClassName builderClassName) {
        final var builder = analysedRecord.utilsClass().createMethod(methodName, claimableOperation);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .addAnnotation(Names.NON_NULL)
            .addJavadoc("Create a blank builder of {@link $T}", analysedRecord.intendedType())
            .addStatement("return $T.$L()", builderClassName, methodName)
            .returns(builderClassName);
        AnnotationSupplier.addGeneratedAnnotation(builder, this);    
    }
}
