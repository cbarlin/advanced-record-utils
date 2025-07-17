package io.github.cbarlin.aru.impl.diff;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULL_MARKED;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.DiffEvaluationMode;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeSpec;

@ServiceProvider
public final class DifferResultsClassGenerator extends DifferVisitor {

    private static final String NON_NULL_CHECK = "$T.requireNonNull($L, $S)";
    private static final String JAVADOC_LAZY = "Results are lazily computed on an as-needed basis";
    private static final String JAVADOC_EAGER = "Results are pre-computed and stored in memory";
    private static final String JAVADOC_NEW_LINE = "\n<p>\n";

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
        AnnotationSupplier.addGeneratedAnnotation(differResultRecordConstructor, this);
        AnnotationSupplier.addGeneratedAnnotation(differResultInterfaceConstructor, this);
        final TypeSpec.Builder classSpec = differResult.builder();
        classSpec.addAnnotation(NULL_MARKED)
            .addOriginatingElement(analysedRecord.typeElement())
            .addJavadoc("The result of a diff between two instances of {@link $T}", analysedRecord.className())
            .addJavadoc(JAVADOC_NEW_LINE)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        differResultRecordConstructor.addJavadoc("Creates a new diff between two instances of {@link $T}", analysedRecord.className())
            .addJavadoc(JAVADOC_NEW_LINE);
        differResultInterfaceConstructor.addJavadoc("Creates a new diff between two instances of {@link $T}", differInterface.className())
            .addJavadoc(JAVADOC_NEW_LINE);

        if (DiffEvaluationMode.EAGER.equals(diffEvaluationMode)) {
            classSpec.addJavadoc(JAVADOC_EAGER);
            differResultRecordConstructor.addJavadoc(JAVADOC_EAGER);
            differResultInterfaceConstructor.addJavadoc(JAVADOC_EAGER);
        } else {
            classSpec.addJavadoc(JAVADOC_LAZY);
            differResultRecordConstructor.addJavadoc(JAVADOC_LAZY);
            differResultInterfaceConstructor.addJavadoc(JAVADOC_LAZY);
        }

        differResultRecordConstructor
            .addStatement(
                NON_NULL_CHECK,
                OBJECTS,
                diffOptionsPrism.originatingElementName(),
                "The originating element cannot be null"
            )
            .addStatement(
                NON_NULL_CHECK,
                OBJECTS,
                diffOptionsPrism.comparedToElementName(),
                "The (potentially) changed element cannot be null"
            )
            .addParameter(
                ParameterSpec.builder(analysedRecord.className(), diffOptionsPrism.originatingElementName(), Modifier.FINAL)
                    .addJavadoc("The originating element of the diff")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(analysedRecord.className(), diffOptionsPrism.comparedToElementName(), Modifier.FINAL)
                    .addJavadoc("The (potentially) changed element of the diff")
                    .build()
            );

        differResultInterfaceConstructor
            .addStatement(
                NON_NULL_CHECK,
                OBJECTS,
                diffOptionsPrism.originatingElementName(),
                "The originating element cannot be null"
            )
            .addStatement(
                NON_NULL_CHECK,
                OBJECTS,
                diffOptionsPrism.comparedToElementName(),
                "The (potentially) changed element cannot be null"
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
