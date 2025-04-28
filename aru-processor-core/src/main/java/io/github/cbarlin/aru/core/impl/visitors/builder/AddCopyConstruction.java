package io.github.cbarlin.aru.core.impl.visitors.builder;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;
import io.github.cbarlin.aru.core.CommonsConstants.Names;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public class AddCopyConstruction extends RecordVisitor {

    public AddCopyConstruction() {
        super(Claims.BUILDER_FROM_EXISTING);
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
        final String methodName = analysedRecord.settings().prism().builderOptions().copyCreationName();
        final ClassName builderClassName = analysedRecord.builderArtifact().className();
        final ParameterSpec parameterSpec = ParameterSpec.builder(analysedRecord.intendedType(), "original", Modifier.FINAL)
            .addJavadoc("The existing instance to copy")
            .build();
        createOnBuilder(analysedRecord, methodName, builderClassName, parameterSpec);
        createOnUtils(analysedRecord, methodName, builderClassName, parameterSpec);
        return true;
    }

    private void createOnBuilder(final AnalysedRecord analysedRecord, final String methodName, final ClassName builderClassName, final ParameterSpec parameterSpec) {
        final var builder = analysedRecord.builderArtifact().createMethod(methodName, claimableOperation);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .addAnnotation(Names.NON_NULL)
            .addParameter(parameterSpec)
            .addJavadoc("Creates a new builder of {@link $T} by copying an existing instance", analysedRecord.intendedType())
            .returns(builderClassName)
            .addStatement("$T.requireNonNull(original, $S)", Names.OBJECTS, "Cannot copy a null instance");

        logTrace(builder, "Copying an existing instance");
        final StringBuilder invocation = new StringBuilder();
        final List<Object> params = new ArrayList<>();
        invocation.append("return $T.$L()");
        params.add(analysedRecord.builderArtifact().className());
        params.add(analysedRecord.settings().prism().builderOptions().emptyCreationName());

        for ( final VariableElement parameter : analysedRecord.intendedConstructor().getParameters()) {
            invocation.append("\n.$L(original.$L())");
            params.add(parameter.getSimpleName().toString());
            params.add(parameter.getSimpleName().toString());
        }
        
        builder.addStatement(invocation.toString(), params.toArray());
        AnnotationSupplier.addGeneratedAnnotation(builder, this);
    }

    private void createOnUtils(final AnalysedRecord analysedRecord, final String methodName, final ClassName builderClassName, final ParameterSpec parameterSpec) {
        final var builder = analysedRecord.utilsClass().createMethod(methodName, claimableOperation);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .addAnnotation(Names.NON_NULL)
            .addParameter(parameterSpec)
            .addJavadoc("Creates a new builder of {@link $T} by copying an existing instance", analysedRecord.intendedType())
            .addStatement("return $T.$L(original)", builderClassName, methodName)
            .returns(builderClassName);
        AnnotationSupplier.addGeneratedAnnotation(builder, this);
    }
}
