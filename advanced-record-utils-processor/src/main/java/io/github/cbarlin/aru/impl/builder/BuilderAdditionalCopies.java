package io.github.cbarlin.aru.impl.builder;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants;
import io.github.cbarlin.aru.impl.wiring.BuilderPerRecordScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;

@Component
@BuilderPerRecordScope
public class BuilderAdditionalCopies extends RecordVisitor {

    BuilderAdditionalCopies(final AnalysedRecord analysedRecord) {
        super(Constants.Claims.BUILDER_ADD_COPY_ADDITIONAL, analysedRecord);
    }

    @Override
    public int specificity() {
        return 0;
    }

    @Override
    protected boolean visitStartOfClassImpl() {
        final String methodName = analysedRecord.settings().prism().builderOptions().copyCreationName();
        final TypeName typeName = ParameterizedTypeName.get(CommonsConstants.Names.OPTIONAL, analysedRecord.intendedType());
        final ParameterSpec optionalTarget = ParameterSpec.builder(typeName, "original", Modifier.FINAL)
                .addJavadoc("An optional existing instance to copy")
                .build();
        final ClassName builderClassName = analysedRecord.builderArtifact().className();
        createOptionalTargetOnBuilder(analysedRecord, methodName, builderClassName, optionalTarget);
        createOptionalTargetOnUtils(analysedRecord, methodName, builderClassName, optionalTarget);
        return true;
    }

    private void createOptionalTargetOnBuilder(final AnalysedRecord analysedRecord, final String methodName, final ClassName builderClassName, final ParameterSpec parameterSpec) {
        final var builder = analysedRecord.builderArtifact().createMethod(methodName, claimableOperation);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addJavadoc("Either copy an existing {@link $T} into a builder, or create a blank builder for it", analysedRecord.intendedType())
                .beginControlFlow("if ($T.nonNull(original) && original.isPresent()) ", CommonsConstants.Names.OBJECTS)
                .addStatement("return $T.$L(original.get())", builderClassName, methodName)
                .nextControlFlow("else")
                .addStatement("return $T.$L()", builderClassName, analysedRecord.settings().prism().builderOptions().emptyCreationName())
                .endControlFlow()
                .returns(builderClassName)
                .addParameter(parameterSpec);
        AnnotationSupplier.addGeneratedAnnotation(builder, this);
    }

    private void createOptionalTargetOnUtils(final AnalysedRecord analysedRecord, final String methodName, final ClassName builderClassName, final ParameterSpec parameterSpec) {
        final var builder = analysedRecord.utilsClass().createMethod(methodName, claimableOperation);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addJavadoc("Either copy an existing {@link $T} into a builder, or create a blank builder for it", analysedRecord.intendedType())
                .addStatement("return $T.$L(original)", builderClassName, methodName)
                .returns(builderClassName)
                .addParameter(parameterSpec);
        AnnotationSupplier.addGeneratedAnnotation(builder, this);
    }
}
