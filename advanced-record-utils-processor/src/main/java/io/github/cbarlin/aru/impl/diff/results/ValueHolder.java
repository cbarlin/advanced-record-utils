package io.github.cbarlin.aru.impl.diff.results;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.diff.DifferVisitor;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public class ValueHolder extends DifferVisitor {

    private static final String ASSIGNMENT = "this.$L = $L.$L()";

    public ValueHolder() {
        super(Claims.DIFFER_VALUE_HOLDING);
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int innerSpecificity() {
        return 0;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final String originalElementName = diffOptionsPrism.originatingElementName() + analysedComponent.nameFirstLetterCaps();
        final String updatedElementName = diffOptionsPrism.comparedToElementName() + analysedComponent.nameFirstLetterCaps();
        differResult.addField(
            FieldSpec.builder(analysedComponent.typeName(), originalElementName, Modifier.PRIVATE, Modifier.FINAL)
                .addJavadoc("The original field named $S", analysedComponent.name())
                .addAnnotation(NULLABLE)
                .build()
        );
        differResult.addField(
            FieldSpec.builder(analysedComponent.typeName(), updatedElementName, Modifier.PRIVATE, Modifier.FINAL)
                .addJavadoc("The (potentially) updated field named $S", analysedComponent.name())
                .addAnnotation(NULLABLE)
                .build()
        );

        differResultRecordConstructor
            .addStatement(
                ASSIGNMENT,
                originalElementName,
                diffOptionsPrism.originatingElementName(),
                analysedComponent.name()
            )
            .addStatement(
                ASSIGNMENT,
                updatedElementName,
                diffOptionsPrism.comparedToElementName(),
                analysedComponent.name()
            );
        differResultInterfaceConstructor
            .addStatement(
                ASSIGNMENT,
                originalElementName,
                diffOptionsPrism.originatingElementName(),
                analysedComponent.name()
            )
            .addStatement(
                ASSIGNMENT,
                updatedElementName,
                diffOptionsPrism.comparedToElementName(),
                analysedComponent.name()
            );

        final MethodSpec.Builder originalMethod = differResult.createMethod(
            originalElementName,
            claimableOperation
        );
        AnnotationSupplier.addGeneratedAnnotation(originalMethod, this);
        originalMethod.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addStatement("return this.$L", originalElementName)
            .addJavadoc("Return the original value for $S", analysedComponent.name())
            .returns(analysedComponent.typeName())
            .addAnnotation(NULLABLE);

        final MethodSpec.Builder updatedMethod = differResult.createMethod(
            updatedElementName,
            claimableOperation
        );
        AnnotationSupplier.addGeneratedAnnotation(updatedMethod, this);
        updatedMethod.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addStatement("return this.$L", updatedElementName)
            .addJavadoc("Return the non-original value for $S", analysedComponent.name())
            .returns(analysedComponent.typeName())
            .addAnnotation(NULLABLE);

        return true;
    }
    
}
