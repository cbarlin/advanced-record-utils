package io.github.cbarlin.aru.impl.diff.results;

import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.diff.DifferVisitor;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public final class EagerNestedDiff extends DifferVisitor {

    public EagerNestedDiff() {
        super(Claims.DIFFER_COMPUTE_CHANGE);
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int innerSpecificity() {
        return 2;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final Optional<ProcessingTarget> targetAnalysedType = analysedComponent.targetAnalysedType();
        final String methodName = hasChangedStaticMethodName(analysedComponent.typeName());
        if (targetAnalysedType.isPresent()) {
            final ProcessingTarget target = targetAnalysedType.get();
            if (target instanceof final AnalysedRecord otherRecord && Boolean.TRUE.equals(otherRecord.settings().prism().diffable()) && (!analysedComponent.requiresUnwrapping())) {
                processComponent(analysedComponent, otherRecord, methodName);
                return true;
            }
        }
        return false;
    }

    private void processComponent(final AnalysedComponent analysedComponent, final AnalysedRecord otherRecord, final String methodName) {
        final ClassName otherResultClass = obtainResultClass(otherRecord).className();
        differResult.addField(
            FieldSpec.builder(otherResultClass, analysedComponent.name(), Modifier.PRIVATE, Modifier.FINAL)
                .addJavadoc("Diff of the field named $S", analysedComponent.name())
                .build()
        );
        for ( final MethodSpec.Builder builder : List.of(differResultRecordConstructor, differResultInterfaceConstructor) ) {
            logTrace(builder, "$S + $S + $S", List.of("Computing the diff of ", analysedComponent.name(), " by using their generated diff utils"));
            builder.addStatement(
                "this.$L = $T.$L(original.$L(), updated.$L())",
                analysedComponent.name(),
                differStaticClass.className(),
                methodName,
                analysedComponent.name(),
                analysedComponent.name()
            );
        }

        final MethodSpec.Builder changedMethod = differResult.createMethod(
            changedMethodName(analysedComponent.name()),
            claimableOperation
        ).addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        AnnotationSupplier.addGeneratedAnnotation(changedMethod, this);
        changedMethod.returns(TypeName.BOOLEAN)
            .addStatement("return this.$L.$L()", analysedComponent.name(), otherRecord.settings().prism().diffOptions().changedAnyMethodName())
            .addJavadoc("Has the $S field changed between the two versions?", analysedComponent.name());

        final MethodSpec.Builder diffMethod = differResult.createMethod(
            diffOptionsPrism.differMethodName() + analysedComponent.nameFirstLetterCaps(),
            claimableOperation
        ).addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        AnnotationSupplier.addGeneratedAnnotation(diffMethod, this);
        diffMethod.returns(otherResultClass)
            .addStatement("return this.$L", analysedComponent.name())
            .addJavadoc("Obtains the diff of the $S field", analysedComponent.name());
    }

}
