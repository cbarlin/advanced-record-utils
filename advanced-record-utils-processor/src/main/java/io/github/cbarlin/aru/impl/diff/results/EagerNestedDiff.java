package io.github.cbarlin.aru.impl.diff.results;

import java.util.List;

import javax.lang.model.element.Modifier;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.diff.DifferVisitor;
import io.github.cbarlin.aru.impl.diff.holders.DiffHolder;
import io.github.cbarlin.aru.impl.types.ComponentTargetingRecord;
import io.github.cbarlin.aru.impl.wiring.DiffPerComponentScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;
import jakarta.inject.Singleton;

@Singleton
@DiffPerComponentScope
@RequiresBean({ComponentTargetingRecord.class})
public final class EagerNestedDiff extends DifferVisitor {

    private final AnalysedRecord otherRecord;

    public EagerNestedDiff(final DiffHolder diffHolder, final ComponentTargetingRecord ctr) {
        super(Claims.DIFFER_COMPUTE_CHANGE, diffHolder);
        this.otherRecord = ctr.target();
    }

    @Override
    protected int innerSpecificity() {
        return 2;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final String methodName = hasChangedStaticMethodName(analysedComponent.typeName());
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
        return true;
    }

}
