package io.github.cbarlin.aru.impl.diff;

import static io.github.cbarlin.aru.impl.Constants.Claims.INTERNAL_MATCHING_IFACE;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.INTERNAL_MATCHING_IFACE_NAME;

import java.util.Locale;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.DiffEvaluationMode;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.AdvancedRecordUtilsPrism.BuilderOptionsPrism;
import io.github.cbarlin.aru.core.AdvancedRecordUtilsPrism.DiffOptionsPrism;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.micronaut.sourcegen.javapoet.MethodSpec;

public abstract class DifferVisitor extends RecordVisitor {
    /*
     * These fields *are* nullable, but functionally when any child class
     *   goes to access them, they will have values.
     * Therefore we will not mark them as `Nullable` (because functionally they aren't) 
     */
    protected ToBeBuilt differInterface;
    protected ToBeBuilt differResult;
    protected BuilderOptionsPrism builderOptionsPrism;
    protected DiffOptionsPrism diffOptionsPrism;
    protected ToBeBuilt matchingInterface;
    protected DiffEvaluationMode diffEvaluationMode;
    protected MethodSpec.Builder differResultConstructor;
    /*
     * TODO: Do we want to have a utility and/or builder class?
     * Pros:
     *  + We can do computation in the utility class only, meaning that the utility class doesn't need to
     *      know about eager vs lazy-with-optional vs lazy-with-vavr vs lazy-with-stablevalues
     *  + We can add more complex logic as needed/wanted without creating a really large constructor
     *  + Don't have to deeply respect user-defined names for variables/params - our code is clearer all around
     *  + The records don't have to inherit the interface for referencing records to get diffs - just use our utility always!
     * 
     * Cons:
     *  - Get the values into the result diff ... how? By a really large, private constructor? I'd prefer to keep fields final...
     *      especially with the JEP draft 8349536 "Prepare to make final mean final" saying the JVM might eventually be able to optimise
     *      final values/fields
     *  - Suppose there is a bug/unexpected behaviour encountered by a user... tracking the code would be harder for them given the 
     *      layers of indirection
     */
    
    @SuppressWarnings({"java:S2637"}) // As mentioned above, they are functionally not null
    protected DifferVisitor(final ClaimableOperation claimableOperation) {
        super(claimableOperation);
    }

    protected abstract boolean innerIsApplicable(final AnalysedRecord analysedRecord);

    protected abstract int innerSpecificity();

    @Override
    public final boolean isApplicable(final AnalysedRecord analysedRecord) {
        if (Boolean.TRUE.equals(analysedRecord.settings().prism().diffable())) {
            matchingInterface = analysedRecord.utilsClassChildInterface(INTERNAL_MATCHING_IFACE_NAME, INTERNAL_MATCHING_IFACE);
            diffOptionsPrism = analysedRecord.settings().prism().diffOptions();
            builderOptionsPrism = analysedRecord.settings().prism().builderOptions();
            diffEvaluationMode = DiffEvaluationMode.LAZY.name().equals(diffOptionsPrism.evaluationMode()) ? DiffEvaluationMode.LAZY : DiffEvaluationMode.EAGER;
            differInterface = analysedRecord.utilsClassChildInterface(diffOptionsPrism.differName(), Claims.DIFFER_IFACE);
            final String resultName = diffOptionsPrism.diffResultPrefix() + analysedRecord.typeSimpleName() + diffOptionsPrism.diffResultSuffix();
            differResult = analysedRecord.utilsClassChildClass(resultName, Claims.DIFFER_RESULT);
            differResultConstructor = differResult.createConstructor();
            return innerIsApplicable(analysedRecord);
        }
        return false;
    }

    @Override
    public final int specificity() {
        return 1 + innerSpecificity();
    }

    protected String changedMethodName(String variableName) {
        return diffOptionsPrism.changedCheckPrefix() + capitalise(variableName) + diffOptionsPrism.changedCheckSuffix();
    }

    protected static String capitalise(final String variableName) {
        return (variableName.length() < 2) ? variableName.toUpperCase(Locale.ROOT)
                : (Character.toUpperCase(variableName.charAt(0)) + variableName.substring(1));
    }
}
