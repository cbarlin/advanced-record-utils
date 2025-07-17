package io.github.cbarlin.aru.impl.builder.collection.eclipse;

import java.util.Locale;

import io.github.cbarlin.aru.core.AdvancedRecordUtilsPrism.BuilderOptionsPrism;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.types.dependencies.EclipseCollectionComponent;

public abstract class EclipseComponentVisitor extends RecordVisitor {

    protected BuilderOptionsPrism builderOptions;

    protected EclipseComponentVisitor(ClaimableOperation claimableOperation) {
        super(claimableOperation);
    }

    public abstract int innerSpecificity();

    @Override
    public final int specificity() {
        return 4 + innerSpecificity();
    }

    public abstract boolean innerVisitComponent(final EclipseCollectionComponent ecc);

    @Override
    protected final boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        if (analysedComponent instanceof EclipseCollectionComponent ecc) {
            builderOptions = ecc.settings().prism().builderOptions();
            return innerVisitComponent(ecc);
        }
        return false;
    }

    public static String addNameMethodName(final AnalysedComponent acc) {
        return acc.settings().prism().builderOptions().adderMethodPrefix() + 
            capitalise(acc.name()) + 
            acc.settings().prism().builderOptions().adderMethodSuffix();
    }

    private static String capitalise(final String variableName) {
        return (variableName.length() < 2) ? variableName.toUpperCase(Locale.ROOT)
                : (Character.toUpperCase(variableName.charAt(0)) + variableName.substring(1));
    }
}
