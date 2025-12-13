package io.github.cbarlin.aru.impl.builder.validation;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.BuilderPerRecordScope;
import io.micronaut.sourcegen.javapoet.ArrayTypeName;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;
import io.micronaut.sourcegen.javapoet.WildcardTypeName;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.impl.Constants.Names.AVAJE_CONSTRAINT_VIOLATION;
import static io.github.cbarlin.aru.impl.Constants.Names.AVAJE_VALIDATOR;

@Component
@BuilderPerRecordScope
@RequiresProperty(value = "validatedBuilder", equalTo = "AVAJE")
public final class AvajeValidatedBuild extends RecordVisitor {

    private static final ParameterizedTypeName CLAZZ_PARAM = ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(TypeName.OBJECT));
    private static final TypeName CLAZZ_ANY = ArrayTypeName.of(CLAZZ_PARAM).annotated(CommonsConstants.NULLABLE_ANNOTATION);

    public AvajeValidatedBuild(final AnalysedRecord analysedRecord) {
        super(Claims.BUILDER_ADD_VALIDATED_BUILD_METHOD, analysedRecord);
    }

    @Override
    public int specificity() {
        return 1;
    }

    @Override
    protected boolean visitStartOfClassImpl() {
        final String methodName = analysedRecord.settings().prism().builderOptions().buildMethodName();
        validatePassedInWithGroups(methodName);
        validateDefaultWithGroups(methodName);
        return true;
    }

    private void validateDefaultWithGroups(final String methodName) {
        final MethodSpec.Builder nArgsBuilder = analysedRecord.builderArtifact()
            .createMethod(methodName + "AndValidate", claimableOperation, CLAZZ_PARAM)
            .addException(AVAJE_CONSTRAINT_VIOLATION)
            .returns(analysedRecord.intendedType())
            .addParameter(
                ParameterSpec.builder(CLAZZ_ANY, "groups", Modifier.FINAL)
                    .addJavadoc("The groups targeted for validation")
                    .build()
            )
            .varargs(true)
            .addJavadoc("Creates a new instance of {@link $T} from the fields set on this builder, validating the result.\n<p>\nUses the default validator", analysedRecord.intendedType())
            .addStatement("return this.$L($T.builder().build(), groups)", methodName, AVAJE_VALIDATOR);

        AnnotationSupplier.addGeneratedAnnotation(nArgsBuilder, this);
    }

    private void validatePassedInWithGroups(final String methodName) {
        final MethodSpec.Builder argedBuilder = analysedRecord.builderArtifact()
            .createMethod(methodName, claimableOperation, AVAJE_VALIDATOR)
            .addException(AVAJE_CONSTRAINT_VIOLATION)
            .returns(analysedRecord.intendedType())
            .addJavadoc("Creates a new instance of {@link $T} from the fields set on this builder, validating the result", analysedRecord.intendedType())
            .addParameter(
                ParameterSpec.builder(AVAJE_VALIDATOR, "validator", Modifier.FINAL)
                    .addJavadoc("The validator to use")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(CLAZZ_ANY, "groups", Modifier.FINAL)
                    .addJavadoc("The groups targeted for validation")
                    .build()
            )
            .varargs(true)
            .addStatement("final $T built = this.$L()", analysedRecord.intendedType(), methodName)
            .addStatement("validator.validate(built, groups)", AVAJE_VALIDATOR)
            .addStatement("return built");
        AnnotationSupplier.addGeneratedAnnotation(argedBuilder, this);
    }

}
