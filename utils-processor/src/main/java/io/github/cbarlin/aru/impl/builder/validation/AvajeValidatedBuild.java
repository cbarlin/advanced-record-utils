package io.github.cbarlin.aru.impl.builder.validation;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.impl.Constants.Names.AVAJE_CONSTRAINT_VIOLATION;
import static io.github.cbarlin.aru.impl.Constants.Names.AVAJE_VALIDATOR;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.ValidationApi;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public class AvajeValidatedBuild extends RecordVisitor {

    public AvajeValidatedBuild() {
        super(Claims.BUILDER_ADD_VALIDATED_BUILD_METHOD);
    }

    @Override
    public int specificity() {
        return 1;
    }

    @Override
    public boolean isApplicable(AnalysedRecord analysedRecord) {
        return ValidationApi.AVAJE.name().equals(analysedRecord.settings().prism().builderOptions().validatedBuilder());
    }

    @Override
    protected boolean visitStartOfClassImpl(AnalysedRecord analysedRecord) {
        final String methodName = analysedRecord.settings().prism().builderOptions().buildMethodName();

        // Start with the one that accepts the argument
        final MethodSpec.Builder argedBuilder = analysedRecord.builderArtifact()
            .createMethod(methodName, claimableOperation, AVAJE_VALIDATOR)
            .addException(AVAJE_CONSTRAINT_VIOLATION)
            .returns(analysedRecord.intendedType())
            .addJavadoc("Creates a new instance of {@link $T} from the fields set on this builder, validating the result", analysedRecord.intendedType())
            .addAnnotation(NON_NULL)
            .addParameter(
                ParameterSpec.builder(AVAJE_VALIDATOR, "validator", Modifier.FINAL)
                    .addJavadoc("The validator to use")
                    .build()
            )
            .addStatement("final $T built = this.$L()", analysedRecord.intendedType(), methodName)
            .addStatement("validator.validate(built)", AVAJE_VALIDATOR)
            .addStatement("return built");
        AnnotationSupplier.addGeneratedAnnotation(argedBuilder, this);

        // Then the no-args version
        final MethodSpec.Builder nArgsBuilder = analysedRecord.builderArtifact()
            .createMethod(methodName + "AndValidate", claimableOperation, AVAJE_CONSTRAINT_VIOLATION)
            .addException(AVAJE_CONSTRAINT_VIOLATION)
            .returns(analysedRecord.intendedType())
            .addJavadoc("Creates a new instance of {@link $T} from the fields set on this builder, validating the result.\n<p>\nUses the default validator", analysedRecord.intendedType())
            .addAnnotation(NON_NULL)
            .addStatement("return this.$L($T.builder().build())", methodName, AVAJE_VALIDATOR);

        AnnotationSupplier.addGeneratedAnnotation(nArgsBuilder, this);

        return true;
    }

}
