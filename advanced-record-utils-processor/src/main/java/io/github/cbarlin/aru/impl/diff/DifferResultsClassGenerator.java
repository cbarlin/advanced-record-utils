package io.github.cbarlin.aru.impl.diff;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULL_MARKED;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.DiffEvaluationMode;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeSpec;

@ServiceProvider
public class DifferResultsClassGenerator extends DifferVisitor {

    public DifferResultsClassGenerator() {
        super(Claims.DIFFER_RESULT);
    }

    @Override
    protected int innerSpecificity() {
        return Integer.MAX_VALUE - 3;
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected boolean visitStartOfClassImpl(final AnalysedRecord analysedRecord) {
        AnnotationSupplier.addGeneratedAnnotation(differResult, this);
        AnnotationSupplier.addGeneratedAnnotation(differResultConstructor, this);
        TypeSpec.Builder classSpec = differResult.builder();
        classSpec.addAnnotation(NULL_MARKED)
            .addOriginatingElement(analysedRecord.typeElement())
            .addJavadoc("The result of a diff between two instances of an {@link $T}", analysedRecord.className())
            .addJavadoc("\n<p>\n")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        differResultConstructor.addJavadoc("Creates a new diff between two instances of an {@link $T}", analysedRecord.className())
            .addJavadoc("\n<p>\n");

        if (DiffEvaluationMode.EAGER.equals(diffEvaluationMode)) {
            classSpec.addJavadoc("Results are pre-computed and stored in memory");
            differResultConstructor.addJavadoc("Results are pre-computed and stored in memory");
        } else {
            classSpec.addJavadoc("Results are lazily computed on an as-needed basis");
            differResultConstructor.addJavadoc("Results are lazily computed on an as-needed basis");
        }

        differResult.addField(
            FieldSpec.builder(differInterface.className(), diffOptionsPrism.originatingElementName(), Modifier.FINAL, Modifier.PRIVATE)
                .addJavadoc("The originating element of the diff")
                .build()
        );

        differResult.addField(
            FieldSpec.builder(differInterface.className(), diffOptionsPrism.comparedToElementName(), Modifier.FINAL, Modifier.PRIVATE)
                .addJavadoc("The (potentially) changed element of the diff")
                .build()
        );

        differResultConstructor
            .addStatement(
                "$T.requireNonNull($L, $S)",
                OBJECTS,
                diffOptionsPrism.originatingElementName(),
                "The originating element cannot be null"
            )
            .addStatement(
                "$T.requireNonNull($L, $S)",
                OBJECTS,
                diffOptionsPrism.comparedToElementName(),
                "The (potentially) changed element cannot be null"
            )
            .addStatement(
                "this.$L = $L",
                diffOptionsPrism.originatingElementName(),
                diffOptionsPrism.originatingElementName()
            )
            .addStatement(
                "this.$L = $L",
                diffOptionsPrism.comparedToElementName(),
                diffOptionsPrism.comparedToElementName()
            )
            .addParameter(
                ParameterSpec.builder(differInterface.className(), diffOptionsPrism.originatingElementName(), Modifier.FINAL)
                    .addJavadoc("The originating element of the diff")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(differInterface.className(), diffOptionsPrism.comparedToElementName(), Modifier.FINAL)
                    .addJavadoc("The (potentially) changed element of the diff")
                    .build()
            );

        return true;
    }
}
