package io.github.cbarlin.aru.impl.diff.results;

import java.util.List;

import javax.lang.model.element.Modifier;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.diff.DifferVisitor;
import io.github.cbarlin.aru.impl.diff.holders.DiffHolder;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHelper;
import io.github.cbarlin.aru.impl.wiring.DiffPerComponentScope;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;
import jakarta.inject.Singleton;

@Singleton
@DiffPerComponentScope
@RequiresBean({CollectionHandlerHelper.class})
public final class EagerCollectionDiffResultCreator extends DifferVisitor {

    public EagerCollectionDiffResultCreator(final DiffHolder diffHolder) {
        super(Claims.DIFFER_COMPUTE_CHANGE, diffHolder);
    }

    @Override
    protected int innerSpecificity() {
        return 5;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent acc) {
        final ToBeBuiltRecord innerRecord = collectionDiffRecord(acc);
        differResult.addField(
            FieldSpec.builder(innerRecord.className(), acc.name(), Modifier.PRIVATE, Modifier.FINAL)
                .addJavadoc("Diff of the field named $S", acc.name())
                .build()
        );
        final String methodName = hasChangedStaticMethodName(acc.typeName());
        for ( final MethodSpec.Builder builder : List.of(differResultRecordConstructor, differResultInterfaceConstructor) ) {
            logTrace(builder, "$S + $S + $S", List.of("Computing the diff of ", acc.name(), " by using their generated diff utils"));
            builder.addStatement(
                "this.$L = $T.$L(original.$L(), updated.$L())",
                acc.name(),
                differStaticClass.className(),
                methodName,
                acc.name(),
                acc.name()
            );
        }

        final MethodSpec.Builder changedMethod = differResult.createMethod(
            changedMethodName(acc.name()),
            claimableOperation
        ).addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        AnnotationSupplier.addGeneratedAnnotation(changedMethod, this);
        changedMethod.returns(TypeName.BOOLEAN)
            .addComment("If there are no added elements and no removed elements, nothing has changed")
            .addStatement("return !(this.$L.addedElements().isEmpty() && this.$L.removedElements().isEmpty())", acc.name(), acc.name())
            .addJavadoc("Has the $S field changed between the two versions?", acc.name());

        final MethodSpec.Builder diffMethod = differResult.createMethod(
            diffOptionsPrism.differMethodName() + acc.nameFirstLetterCaps(),
            claimableOperation
        ).addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        AnnotationSupplier.addGeneratedAnnotation(diffMethod, this);
        diffMethod.returns(innerRecord.className())
            .addStatement("return this.$L", acc.name())
            .addJavadoc("Obtains the diff of the $S field", acc.name());

        return true;
    }
    
}
