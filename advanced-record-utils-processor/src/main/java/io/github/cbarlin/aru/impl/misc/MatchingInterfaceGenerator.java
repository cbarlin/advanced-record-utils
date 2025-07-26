package io.github.cbarlin.aru.impl.misc;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.wiring.BasePerRecordScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import jakarta.inject.Singleton;

@Singleton
@BasePerRecordScope
public final class MatchingInterfaceGenerator extends RecordVisitor {

    private final MatchingInterface matchingInterface;

    public MatchingInterfaceGenerator(final MatchingInterface matchingInterface) {
        super(matchingInterface.claimableOperation(), matchingInterface.analysedRecord());
        this.matchingInterface = matchingInterface;
    }

    @Override
    public int specificity() {
        return Integer.MAX_VALUE - 1;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final MethodSpec.Builder methodBuilder = matchingInterface.createMethod(analysedComponent.name(), claimableOperation, analysedComponent)
            .returns(analysedComponent.typeName())
            .addModifiers(Modifier.ABSTRACT);

        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        return true;
    }
}
