package io.github.cbarlin.aru.impl.diff.results;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

import java.util.List;

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
public class StandardEagerDiffCheck extends DifferVisitor {

    public StandardEagerDiffCheck() {
        super(Claims.DIFFER_COMPUTE_CHANGE);
    }

    @Override
    protected boolean innerIsApplicable(AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int innerSpecificity() {
        return 0;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        differResult.addField(
            FieldSpec.builder(TypeName.BOOLEAN, analysedComponent.name(), Modifier.PRIVATE, Modifier.FINAL)
                .addJavadoc("Has the field named $S changed?", analysedComponent.name())
                .build()
        );
        logTrace(differResultConstructor, "$S + $S + $S", List.of("Computing the diff of ", analysedComponent.name(), " by calling Objects.equals"));
        differResultConstructor.addStatement(
            "this.$L = $T.equals($L.$L(), $L.$L())",
            analysedComponent.name(),
            OBJECTS,
            diffOptionsPrism.originatingElementName(),
            analysedComponent.name(),
            diffOptionsPrism.comparedToElementName(),
            analysedComponent.name()
        );

        final MethodSpec.Builder changedMethod = differResult.createMethod(
            changedMethodName(analysedComponent.name()),
            claimableOperation
        ).addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        AnnotationSupplier.addGeneratedAnnotation(changedMethod, this);
        changedMethod.returns(TypeName.BOOLEAN)
            .addStatement("return this.$L", analysedComponent.name())
            .addJavadoc("Has the $S field changed between the two versions?", analysedComponent.name());

        final MethodSpec.Builder originalMethod = differResult.createMethod(
            diffOptionsPrism.originatingElementName() + analysedComponent.nameFirstLetterCaps(),
            claimableOperation
        );
        AnnotationSupplier.addGeneratedAnnotation(originalMethod, this);
        originalMethod.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addStatement("return this.$L.$L()", diffOptionsPrism.originatingElementName(), analysedComponent.name())
            .addJavadoc("Return the original value for $S", analysedComponent.name())
            .returns(analysedComponent.typeName())
            .addAnnotation(NULLABLE);

        final MethodSpec.Builder updatedMethod = differResult.createMethod(
            diffOptionsPrism.comparedToElementName() + analysedComponent.nameFirstLetterCaps(),
            claimableOperation
        );
        AnnotationSupplier.addGeneratedAnnotation(updatedMethod, this);
        updatedMethod.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addStatement("return this.$L.$L()", diffOptionsPrism.comparedToElementName(), analysedComponent.name())
            .addJavadoc("Return the non-original value for $S", analysedComponent.name())
            .returns(analysedComponent.typeName())
            .addAnnotation(NULLABLE);

        return true;
    }
    
}
