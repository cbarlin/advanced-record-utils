package io.github.cbarlin.aru.impl.diff.utils;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.COLLECTORS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.impl.Constants.Names.PREDICATE;
import static io.github.cbarlin.aru.impl.Constants.Names.SET;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.diff.DifferVisitor;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;

@ServiceProvider
public class SetDiffCreation extends DifferVisitor {
    
    public SetDiffCreation() {
        super(Claims.DIFFER_UTILS_COMPUTE_CHANGE);
    }

    private final Set<String> processedSpecs = HashSet.newHashSet(20);

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int innerSpecificity() {
        return 2;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        if ((!(analysedComponent instanceof final AnalysedCollectionComponent acc)) || (!acc.isSet())) {
            return false;
        }
        final String methodName = hasChangedStaticMethodName(analysedComponent.typeName());
        if (!processedSpecs.add(methodName)) {
            return true;
        }
        final MethodSpec.Builder builder = differStaticClass.createMethod(methodName, claimableOperation);
        methodParamsReturn(analysedComponent, acc, builder);

        final ParameterizedTypeName setPtn = ParameterizedTypeName.get(SET, acc.unNestedPrimaryTypeName());
        builder.addComment("Filter elements by comparing against the other set")
            .addStatement(
                """
                    final $T added = updated.stream()
                        .filter($T.not(original::contains))
                        .collect($T.toUnmodifiableSet())
                """,
                setPtn,
                PREDICATE,
                COLLECTORS
            )
            .addStatement(
                """
                    final $T removed = original.stream()
                        .filter($T.not(updated::contains))
                        .collect($T.toUnmodifiableSet())
                """,
                setPtn,
                PREDICATE,
                COLLECTORS
            )
            .addStatement(
                """
                    final $T common = original.stream()
                        .filter(updated::contains)
                        .collect($T.toUnmodifiableSet())
                """,
                setPtn,
                COLLECTORS
            );
        builder.addStatement("return new $T(added, removed, common)", collectionDiffRecord(acc).className());
        return true;
    }

    private void methodParamsReturn(AnalysedComponent analysedComponent, AnalysedCollectionComponent acc,
            final MethodSpec.Builder builder) {
        AnnotationSupplier.addGeneratedAnnotation(builder, this);
        builder.addModifiers(Modifier.FINAL, Modifier.STATIC)
                .returns(collectionDiffRecord(acc).className())
                .addParameter(
                    ParameterSpec.builder(analysedComponent.typeName(), "original", Modifier.FINAL)
                        .addAnnotation(NULLABLE)
                        .build()   
                )
                .addParameter(
                    ParameterSpec.builder(analysedComponent.typeName(), "updated", Modifier.FINAL)
                        .addAnnotation(NULLABLE)
                        .build()   
                );
    }
}
