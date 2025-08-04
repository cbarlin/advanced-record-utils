package io.github.cbarlin.aru.impl.diff.results;

import java.util.List;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.diff.DifferVisitor;
import io.github.cbarlin.aru.impl.diff.holders.DiffHolder;
import io.github.cbarlin.aru.impl.wiring.DiffPerRecordScope;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;
import jakarta.inject.Singleton;

@Singleton
@DiffPerRecordScope
public final class EagerStandardFieldChanged extends DifferVisitor {
    public EagerStandardFieldChanged(final DiffHolder diffHolder) {
        super(Claims.DIFFER_COMPUTE_CHANGE, diffHolder);
    }

    @Override
    protected int innerSpecificity() {
        return 0;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        differResult.addField(
            FieldSpec.builder(TypeName.BOOLEAN, analysedComponent.name(), Modifier.PRIVATE, Modifier.FINAL)
                .addJavadoc("Has the field named $S changed?", analysedComponent.name())
                .build()
        );
        for ( final MethodSpec.Builder builder : List.of(differResultRecordConstructor, differResultInterfaceConstructor) ) {
            logTrace(builder, "$S + $S + $S", List.of("Computing the diff of ", analysedComponent.name(), " by calling Objects.equals"));
            builder.addStatement(
                "this.$L = $T.$L(original.$L(), updated.$L())",
                analysedComponent.name(),
                differStaticClass.className(),
                hasChangedStaticMethodName(analysedComponent.typeName()),
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
            .addStatement("return this.$L", analysedComponent.name())
            .addJavadoc("Has the $S field changed between the two versions?", analysedComponent.name());
            
        return true;
    }

}
