package io.github.cbarlin.aru.core.impl.visitors.builder;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.core.wiring.CorePerRecordScope;
import io.micronaut.sourcegen.javapoet.ClassName;

import javax.lang.model.element.Modifier;

@Component
@CorePerRecordScope
public final class AddEmptyConstruction extends RecordVisitor {

    public AddEmptyConstruction(final AnalysedRecord analysedRecord) {
        super(Claims.CORE_BUILDER_FROM_NOTHING, analysedRecord);
    }

    @Override
    public int specificity() {
        return 0;
    }

    @Override
    protected boolean visitStartOfClassImpl() {
        final String methodName = analysedRecord.settings().prism().builderOptions().emptyCreationName();
        final ClassName builderClassName = analysedRecord.builderArtifact().className();
        createOnBuilder(analysedRecord, methodName, builderClassName);
        createOnUtils(analysedRecord, methodName, builderClassName);
        return true;
    }

    private void createOnBuilder(final AnalysedRecord analysedRecord, final String methodName, final ClassName builderClassName) {
        final var builder = analysedRecord.builderArtifact().createMethod(methodName, claimableOperation);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .addJavadoc("Create a blank builder of {@link $T}", analysedRecord.intendedType())
            .addStatement("return new $T()", builderClassName)
            .returns(builderClassName);
        AnnotationSupplier.addGeneratedAnnotation(builder, this);   
    }

    private void createOnUtils(final AnalysedRecord analysedRecord, final String methodName, final ClassName builderClassName) {
        final var builder = analysedRecord.utilsClass().createMethod(methodName, claimableOperation);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .addJavadoc("Create a blank builder of {@link $T}", analysedRecord.intendedType())
            .addStatement("return $T.$L()", builderClassName, methodName)
            .returns(builderClassName);
        AnnotationSupplier.addGeneratedAnnotation(builder, this);    
    }
}
