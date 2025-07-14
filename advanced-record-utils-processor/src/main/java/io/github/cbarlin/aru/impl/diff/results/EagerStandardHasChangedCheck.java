package io.github.cbarlin.aru.impl.diff.results;

import java.util.ArrayList;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.diff.DifferVisitor;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public class EagerStandardHasChangedCheck extends DifferVisitor {

    public EagerStandardHasChangedCheck() {
        super(Claims.DIFFER_OVERALL_HAS_CHANGED);
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
    protected void visitEndOfClassImpl(final AnalysedRecord analysedRecord) {
        differResult.addField(
            FieldSpec.builder(TypeName.BOOLEAN, "__overallChanged", Modifier.PRIVATE, Modifier.FINAL)
                .addJavadoc("Has any field changed in this diff?")
                .build()
        );
        // This should just OR all the existing ones!
        final StringBuilder assignment = new StringBuilder("this.__overallChanged = ");
        final ArrayList<Object> checks = new ArrayList<>(analysedRecord.components().size());
        for (final AnalysedComponent analysedComponent : analysedRecord.components()) {
            // Invoke the methods rather than use the variable - the variable version may not be a boolean!
            assignment.append("this.$L() || ");
            checks.add(changedMethodName(analysedComponent.name()));
        }
        // If there are no components then it's false
        //   and if there are then we've ended with an "||"
        assignment.append("false");
        // Add the calculations
        differResultRecordConstructor.addStatement(
            assignment.toString(),
            checks.toArray()
        );
        differResultInterfaceConstructor.addStatement(
            assignment.toString(),
            checks.toArray()
        );

        final MethodSpec.Builder changedMethod = differResult.createMethod(
            diffOptionsPrism.changedAnyMethodName(),
            claimableOperation
        ).addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        AnnotationSupplier.addGeneratedAnnotation(changedMethod, this);
        changedMethod.returns(TypeName.BOOLEAN)
            .addStatement("return this.__overallChanged")
            .addJavadoc("Has any field changed in this diff?");
    }
}
