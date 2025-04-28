package io.github.cbarlin.aru.impl.builder.validation;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.SET;
import static io.github.cbarlin.aru.impl.Constants.Names.BI_CONSUMER;
import static io.github.cbarlin.aru.impl.Constants.Names.CONSTRAINT_VIOLATION;
import static io.github.cbarlin.aru.impl.Constants.Names.CONSUMER;
import static io.github.cbarlin.aru.impl.Constants.Names.JAKARTA_VALIDATOR;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.ValidationApi;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;

@ServiceProvider
public class JakartaValidatedBuild extends RecordVisitor {

    public JakartaValidatedBuild() {
        super(Claims.BUILDER_ADD_VALIDATED_BUILD_METHOD);
    }

    @Override
    public int specificity() {
        return 1;
    }

    @Override
    public boolean isApplicable(AnalysedRecord analysedRecord) {
        return ValidationApi.JAKARTA_PLAIN.name().equals(analysedRecord.settings().prism().builderOptions().validatedBuilder());
    }

    @Override
    protected boolean visitStartOfClassImpl(AnalysedRecord analysedRecord) {
        final ParameterizedTypeName constraintType = ParameterizedTypeName.get(CONSTRAINT_VIOLATION, analysedRecord.intendedType());
        final ParameterizedTypeName setOfViolations = ParameterizedTypeName.get(SET, constraintType);
        final String methodName = analysedRecord.settings().prism().builderOptions().buildMethodName();

        // Single consumer version
        final ParameterizedTypeName singleConsumer = ParameterizedTypeName.get(CONSUMER, setOfViolations);
        final MethodSpec.Builder consumerBuilder = analysedRecord.builderArtifact()
            .createMethod(methodName, claimableOperation, singleConsumer)
            .addAnnotation(NON_NULL)
            .addJavadoc("Creates a new instance of {@link $T} from the fields set on this builder, validating the result\n<p>\nThe consumer is only called if there are results", analysedRecord.intendedType())
            .returns(analysedRecord.intendedType())
            .addParameter(
                ParameterSpec.builder(JAKARTA_VALIDATOR, "validator", Modifier.FINAL)
                    .addJavadoc("The validator to use")
                    .addAnnotation(NON_NULL)
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(singleConsumer, "resultConsumer", Modifier.FINAL)
                    .addJavadoc("A consumer to call if there are {@link $T} results. Not called if there are no results", CONSTRAINT_VIOLATION)
                    .addAnnotation(NON_NULL)
                    .build()
            )
            .addStatement("final $T built = this.$L()", analysedRecord.intendedType(), methodName)
            .addStatement("final $T validationResults = validator.validate(built)", setOfViolations)
            .beginControlFlow("if ($T.nonNull(validationResults) && (!validationResults.isEmpty))", OBJECTS)
            .addStatement("resultConsumer.accept(validationResults)")
            .endControlFlow()
            .addStatement("return built");
        AnnotationSupplier.addGeneratedAnnotation(consumerBuilder, this);

        // BiConsumer version
        final ParameterizedTypeName duoConsumer = ParameterizedTypeName.get(BI_CONSUMER, setOfViolations, analysedRecord.intendedType());
        final MethodSpec.Builder biConsumerBuilder = analysedRecord.builderArtifact()
            .createMethod(methodName, claimableOperation, duoConsumer)
            .addAnnotation(NON_NULL)
            .addJavadoc("Creates a new instance of {@link $T} from the fields set on this builder, validating the result\n<p>\nUnlike the single consumer acceptor, this consumer is always called", analysedRecord.intendedType())
            .returns(analysedRecord.intendedType())
            .addParameter(
                ParameterSpec.builder(JAKARTA_VALIDATOR, "validator", Modifier.FINAL)
                    .addJavadoc("The validator to use")
                    .addAnnotation(NON_NULL)
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(duoConsumer, "resultConsumer", Modifier.FINAL)
                    .addJavadoc("A consumer to call if with the {@link $T} results and the built object. Will be called even if there are no results", CONSTRAINT_VIOLATION)
                    .addAnnotation(NON_NULL)
                    .build()
            )
            .addStatement("final $T built = this.$L()", analysedRecord.intendedType(), methodName)
            .addStatement("final $T validationResults = validator.validate(built)", setOfViolations)
            .addStatement("resultConsumer.accept(validationResults, built)")
            .addStatement("return built");

        AnnotationSupplier.addGeneratedAnnotation(biConsumerBuilder, this);

        return true;
    }
}
