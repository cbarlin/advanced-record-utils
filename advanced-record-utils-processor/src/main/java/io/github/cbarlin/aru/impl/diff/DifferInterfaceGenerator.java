package io.github.cbarlin.aru.impl.diff;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULL_MARKED;
import static io.github.cbarlin.aru.impl.Constants.Claims.INTERNAL_MATCHING_IFACE;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.INTERNAL_MATCHING_IFACE_NAME;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.DiffEvaluationMode;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public class DifferInterfaceGenerator extends DifferVisitor {

    public DifferInterfaceGenerator() {
        super(Claims.DIFFER_IFACE);
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
        if (!analysedRecord.isClaimed(io.github.cbarlin.aru.core.CommonsConstants.Claims.CORE_BUILDER_CLASS)) {
            APContext.messager().printError("The builder wasn't claimed before the Differ!");
        }
        AnnotationSupplier.addGeneratedAnnotation(differInterface, this);
        differInterface.builder()
            .addAnnotation(NULL_MARKED)
            .addOriginatingElement(analysedRecord.typeElement())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addJavadoc("Interface for a record that can be diffed against itself")
            .addSuperinterface(
                  analysedRecord.utilsClassChildInterface(INTERNAL_MATCHING_IFACE_NAME, INTERNAL_MATCHING_IFACE)
                    .className()
            );

        final MethodSpec.Builder builder = differInterface.createMethod(diffOptionsPrism.differMethodName(), claimableOperation)
            .addAnnotation(NON_NULL)
            .addModifiers(Modifier.DEFAULT)
            .addParameter(
                ParameterSpec.builder(differInterface.className(), diffOptionsPrism.comparedToElementName(), Modifier.FINAL)
                    .addAnnotation(NON_NULL)
                    .build()
            )
            .returns(differResult.className())
            .addStatement("return new $T(this, $L)", differResult.className(), diffOptionsPrism.comparedToElementName())
            .addJavadoc("Generate the diff between this instance ($S) and the provided instance ($S)", diffOptionsPrism.originatingElementName(), diffOptionsPrism.comparedToElementName())
            .addJavadoc("\n<p>\n");
        
        if (DiffEvaluationMode.EAGER.equals(diffEvaluationMode)) {
            builder.addJavadoc("Diff is computed as soon as this method is called");
        } else {
            builder.addJavadoc("Diff is computed as requested");
        }
        builder.addJavadoc("\n@return The result of the diff");

        AnnotationSupplier.addGeneratedAnnotation(builder, this);

        return true;
    }
}
