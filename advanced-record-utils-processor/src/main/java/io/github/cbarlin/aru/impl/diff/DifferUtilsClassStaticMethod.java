package io.github.cbarlin.aru.impl.diff;

import javax.lang.model.element.Modifier;

import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.diff.holders.DiffHolder;
import io.github.cbarlin.aru.impl.wiring.DiffPerRecordScope;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import jakarta.inject.Singleton;

@Singleton
@DiffPerRecordScope
@RequiresProperty(value = "diffOptions.staticMethodsAddedToUtils", equalTo = "true")
public final class DifferUtilsClassStaticMethod extends DifferVisitor {

    public DifferUtilsClassStaticMethod(final DiffHolder diffHolder) {
        super(Claims.DIFFER_STATIC_UTILS_METHOD, diffHolder);
    }

    @Override
    protected int innerSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitStartOfClassImpl() {
        final var builder = analysedRecord.utilsClass().createMethod(diffOptionsPrism.differMethodName(), claimableOperation)
            .returns(differResult.className())
            .addModifiers(Modifier.FINAL, Modifier.STATIC)
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
