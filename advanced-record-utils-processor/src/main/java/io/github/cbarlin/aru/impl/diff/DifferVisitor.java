package io.github.cbarlin.aru.impl.diff;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.DiffEvaluationMode;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.diff.holders.DiffHolder;
import io.github.cbarlin.aru.impl.diff.holders.DiffInterfaceClass;
import io.github.cbarlin.aru.impl.diff.holders.DiffResultsClass;
import io.github.cbarlin.aru.impl.diff.holders.DiffStaticClass;
import io.github.cbarlin.aru.impl.misc.MatchingInterface;
import io.github.cbarlin.aru.prism.prison.BuilderOptionsPrism;
import io.github.cbarlin.aru.prism.prison.DiffOptionsPrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import java.util.Locale;

public abstract class DifferVisitor extends RecordVisitor {

    private static final String HAS_CHANGED_STATIC_METHOD_FMT = "has%sChanged";

    protected final DiffOptionsPrism diffOptionsPrism;
    protected final DiffInterfaceClass diffInterfaceClass;
    protected final DiffResultsClass differResult;
    protected final DiffStaticClass differStaticClass;
    protected final MethodSpec.Builder differResultRecordConstructor;
    protected final MethodSpec.Builder differResultInterfaceConstructor;
    protected final BuilderOptionsPrism builderOptionsPrism;
    protected final MatchingInterface matchingInterface;
    protected final DiffEvaluationMode diffEvaluationMode;

    /*
     * These fields *are* nullable, but functionally when any child class
     *   goes to access them, they will have values.
     * Therefore we will not mark them as `Nullable` (because functionally they aren't) 
     */
    
    @SuppressWarnings({"java:S2637"})
    protected DifferVisitor(final ClaimableOperation claimableOperation, final DiffHolder diffHolder) {
        super(claimableOperation, diffHolder.analysedRecord());
        this.diffOptionsPrism = diffHolder.diffOptionsPrism();
        this.diffInterfaceClass = diffHolder.interfaceClass();
        this.differResult = diffHolder.resultsClass();
        this.differStaticClass = diffHolder.staticClass();
        this.differResultRecordConstructor = diffHolder.resultsClass().recordConstructor();
        this.differResultInterfaceConstructor = diffHolder.resultsClass().interfaceConstructor();
        this.matchingInterface = diffHolder.matchingInterface();
        this.builderOptionsPrism = diffHolder.builderOptionsPrism();
        this.diffEvaluationMode = DiffEvaluationMode.LAZY.name().equals(diffOptionsPrism.evaluationMode()) ? DiffEvaluationMode.LAZY : DiffEvaluationMode.EAGER;
    }

    protected abstract int innerSpecificity();

    @Override
    public final int specificity() {
        return 1 + innerSpecificity();
    }

    protected String changedMethodName(final String variableName) {
        return diffOptionsPrism.changedCheckPrefix() + capitalise(variableName) + diffOptionsPrism.changedCheckSuffix();
    }

    protected ToBeBuiltRecord collectionDiffRecord(final AnalysedComponent acc) {
        final String innerClassName = "DiffOf" + typeNameToPartialMethodName(acc.typeName());
        return (ToBeBuiltRecord) differResult.childRecordArtifact(innerClassName, Claims.DIFFER_COLLECTION_RESULT);
    }

    protected static ToBeBuilt obtainResultClass(final AnalysedRecord analysedRecord) {
        final DiffOptionsPrism diffOptionsPrism = analysedRecord.settings().prism().diffOptions();
        final String resultName = diffOptionsPrism.diffResultPrefix() + analysedRecord.typeSimpleName() + diffOptionsPrism.diffResultSuffix();
        return analysedRecord.utilsClassChildClass(resultName, Claims.DIFFER_RESULT);
    }

    protected static String capitalise(final String variableName) {
        return (variableName.length() < 2) ? variableName.toUpperCase(Locale.ROOT)
                : (Character.toUpperCase(variableName.charAt(0)) + variableName.substring(1));
    }

    protected static String hasChangedStaticMethodName(final TypeName originalTypeName) {
        return HAS_CHANGED_STATIC_METHOD_FMT.formatted(typeNameToPartialMethodName(originalTypeName));
    }
}
