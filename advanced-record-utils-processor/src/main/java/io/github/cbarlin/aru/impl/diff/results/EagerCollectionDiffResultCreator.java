package io.github.cbarlin.aru.impl.diff.results;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.diff.DifferVisitor;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public final class EagerCollectionDiffResultCreator extends DifferVisitor {

    private final Set<String> processedSpecs = HashSet.newHashSet(20);

    public EagerCollectionDiffResultCreator() {
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
        if (analysedComponent instanceof final AnalysedCollectionComponent acc) {
            final ToBeBuiltRecord innerRecord = collectionDiffRecord(acc);
            if (processedSpecs.add(innerRecord.className().simpleName())) {
                createRecord(acc, innerRecord);
            }
            differResult.addField(
                FieldSpec.builder(innerRecord.className(), acc.name(), Modifier.PRIVATE, Modifier.FINAL)
                    .addJavadoc("Diff of the field named $S", acc.name())
                    .build()
            );
            final String methodName = hasChangedStaticMethodName(analysedComponent.typeName());
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
                .addComment("If there are no added elements and no removed elements, nothing has changed")
                .addStatement("return !(this.$L.addedElements().isEmpty() && this.$L.removedElements().isEmpty())", analysedComponent.name(), analysedComponent.name())
                .addJavadoc("Has the $S field changed between the two versions?", analysedComponent.name());

            final MethodSpec.Builder diffMethod = differResult.createMethod(
                diffOptionsPrism.differMethodName() + analysedComponent.nameFirstLetterCaps(),
                claimableOperation
            ).addModifiers(Modifier.PUBLIC, Modifier.FINAL);
            AnnotationSupplier.addGeneratedAnnotation(diffMethod, this);
            diffMethod.returns(innerRecord.className())
                .addStatement("return this.$L", analysedComponent.name())
                .addJavadoc("Obtains the diff of the $S field", analysedComponent.name());

            return true;
        }
        return false;
    }


    private void createRecord(
            final AnalysedCollectionComponent acc,
            final ToBeBuiltRecord builder
    ) {
        // Excellent, now create a record
        AnnotationSupplier.addGeneratedAnnotation(builder, this);
        builder.builder()
            .addOriginatingElement(acc.parentRecord().typeElement())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addJavadoc("A record containing the difference between two collections");
        builder.addParameterSpec(
                ParameterSpec.builder(acc.typeName(), "addedElements")
                    .addJavadoc("The elements added to the collection")
                    .build()  
            )
            .addParameterSpec(
                ParameterSpec.builder(acc.typeName(), "removedElements")
                    .addJavadoc("The elements removed from the collection")
                    .build()  
            )
            .addParameterSpec(
                ParameterSpec.builder(acc.typeName(), "elementsInCommon")
                    .addJavadoc("The elements in common between the two instances")
                    .build()  
            );
    }
    
}
