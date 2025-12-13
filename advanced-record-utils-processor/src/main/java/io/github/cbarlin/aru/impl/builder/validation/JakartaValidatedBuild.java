package io.github.cbarlin.aru.impl.builder.validation;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.BuilderPerRecordScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.SET;
import static io.github.cbarlin.aru.impl.Constants.Names.BI_CONSUMER;
import static io.github.cbarlin.aru.impl.Constants.Names.CONSTRAINT_VIOLATION;
import static io.github.cbarlin.aru.impl.Constants.Names.CONSUMER;
import static io.github.cbarlin.aru.impl.Constants.Names.JAKARTA_VALIDATOR;

@Component
@BuilderPerRecordScope
@RequiresProperty(value = "validatedBuilder", equalTo = "JAKARTA_PLAIN")
public final class JakartaValidatedBuild extends RecordVisitor {

    public JakartaValidatedBuild(final AnalysedRecord analysedRecord) {
        super(Claims.BUILDER_ADD_VALIDATED_BUILD_METHOD, analysedRecord);
    }

    @Override
    public int specificity() {
        return 1;
    }

    @Override
    protected boolean visitStartOfClassImpl() {
        final ParameterizedTypeName constraintType = ParameterizedTypeName.get(CONSTRAINT_VIOLATION, analysedRecord.intendedType());
        final ParameterizedTypeName setOfViolations = ParameterizedTypeName.get(SET, constraintType);
        final String methodName = analysedRecord.settings().prism().builderOptions().buildMethodName();

        // Single consumer version
        final ParameterizedTypeName singleConsumer = ParameterizedTypeName.get(CONSUMER, setOfViolations);
        final MethodSpec.Builder consumerBuilder = analysedRecord.builderArtifact()
            .createMethod(methodName, claimableOperation, singleConsumer)
            .addJavadoc("Creates a new instance of {@link $T} from the fields set on this builder, validating the result\n<p>\nThe consumer is only called if there are results", analysedRecord.intendedType())
            .returns(analysedRecord.intendedType())
            .addParameter(
                ParameterSpec.builder(JAKARTA_VALIDATOR, "validator", Modifier.FINAL)
                    .addJavadoc("The validator to use")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(singleConsumer, "resultConsumer", Modifier.FINAL)
                    .addJavadoc("A consumer to call if there are {@link $T} results. Not called if there are no results", CONSTRAINT_VIOLATION)
                    .build()
            )
            .addStatement("final $T built = this.$L()", analysedRecord.intendedType(), methodName)
            .addStatement("final $T validationResults = validator.validate(built)", setOfViolations)
            .beginControlFlow("if ($T.nonNull(validationResults) && (!validationResults.isEmpty()))", OBJECTS)
            .addStatement("resultConsumer.accept(validationResults)")
            .endControlFlow()
            .addStatement("return built");
        AnnotationSupplier.addGeneratedAnnotation(consumerBuilder, this);

        // BiConsumer version
        final ParameterizedTypeName duoConsumer = ParameterizedTypeName.get(BI_CONSUMER, setOfViolations, analysedRecord.intendedType());
        final MethodSpec.Builder biConsumerBuilder = analysedRecord.builderArtifact()
            .createMethod(methodName, claimableOperation, duoConsumer)
            .addJavadoc("Creates a new instance of {@link $T} from the fields set on this builder, validating the result\n<p>\nUnlike the single consumer acceptor, this consumer is always called", analysedRecord.intendedType())
            .returns(analysedRecord.intendedType())
            .addParameter(
                ParameterSpec.builder(JAKARTA_VALIDATOR, "validator", Modifier.FINAL)
                    .addJavadoc("The validator to use")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(duoConsumer, "resultConsumer", Modifier.FINAL)
                    .addJavadoc("A consumer to call if with the {@link $T} results and the built object. Will be called even if there are no results", CONSTRAINT_VIOLATION)
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
