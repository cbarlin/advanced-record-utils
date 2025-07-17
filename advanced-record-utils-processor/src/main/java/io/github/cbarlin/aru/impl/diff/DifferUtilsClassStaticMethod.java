package io.github.cbarlin.aru.impl.diff;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public final class DifferUtilsClassStaticMethod extends DifferVisitor {

    public DifferUtilsClassStaticMethod() {
        super(Claims.DIFFER_STATIC_UTILS_METHOD);
    }

    @Override
    protected boolean innerIsApplicable(AnalysedRecord analysedRecord) {
        return Boolean.TRUE.equals(diffOptionsPrism.staticMethodsAddedToUtils());
    }

    @Override
    protected int innerSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitStartOfClassImpl(AnalysedRecord analysedRecord) {
        final var builder = analysedRecord.utilsClass().createMethod(diffOptionsPrism.differName(), claimableOperation)
            .returns(differResult.className())
            .addJavadoc("Perform a diff on two instances of {@link $T}", analysedRecord.className())
            .addParameter(
                ParameterSpec.builder(analysedRecord.className(), diffOptionsPrism.originatingElementName(), Modifier.FINAL)
                    .addJavadoc("The originating element")
                    .build()   
            )
            .addParameter(
                ParameterSpec.builder(analysedRecord.className(), diffOptionsPrism.comparedToElementName(), Modifier.FINAL)
                    .addJavadoc("The (possibly) updated element")
                    .build()   
            )
            .addStatement("return new $T($L, $L)", differResult.className(), diffOptionsPrism.originatingElementName(), diffOptionsPrism.comparedToElementName());
        AnnotationSupplier.addGeneratedAnnotation(builder, this);
        return true;
    }

}
