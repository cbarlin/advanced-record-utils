package io.github.cbarlin.aru.impl.diff;

import static io.github.cbarlin.aru.impl.Constants.Claims.INTERNAL_MATCHING_IFACE;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.DIFFER_UTILS_CLASS;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.INTERNAL_MATCHING_IFACE_NAME;

import java.util.Locale;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.DiffEvaluationMode;
import io.github.cbarlin.aru.core.AdvancedRecordUtilsPrism.BuilderOptionsPrism;
import io.github.cbarlin.aru.core.AdvancedRecordUtilsPrism.DiffOptionsPrism;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.micronaut.sourcegen.javapoet.ArrayTypeName;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

public abstract class DifferVisitor extends RecordVisitor {

    private static final String HAS_CHANGED_STATIC_METHOD_FMT = "has%sChanged";

    /*
     * These fields *are* nullable, but functionally when any child class
     *   goes to access them, they will have values.
     * Therefore we will not mark them as `Nullable` (because functionally they aren't) 
     */
    protected ToBeBuilt differInterface;
    protected ToBeBuilt differResult;
    protected ToBeBuilt differStaticClass;
    protected BuilderOptionsPrism builderOptionsPrism;
    protected DiffOptionsPrism diffOptionsPrism;
    protected ToBeBuilt matchingInterface;
    protected DiffEvaluationMode diffEvaluationMode;
    protected MethodSpec.Builder differResultRecordConstructor;
    protected MethodSpec.Builder differResultInterfaceConstructor; 
    
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
            differStaticClass = analysedRecord.utilsClassChildClass(DIFFER_UTILS_CLASS, Claims.DIFFER_UTILS);
            differResult = obtainResultClass(analysedRecord);
            differResultRecordConstructor = differResult.createConstructor();
            differResultInterfaceConstructor = differResult.createMethod("<init>", Claims.DIFFER_IFACE);
            return innerIsApplicable(analysedRecord);
        }
        return false;
    }

    @Override
    public final int specificity() {
        return 1 + innerSpecificity();
    }

    protected String changedMethodName(final String variableName) {
        return diffOptionsPrism.changedCheckPrefix() + capitalise(variableName) + diffOptionsPrism.changedCheckSuffix();
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

    @SuppressWarnings({"java:S6880"}) // There is a ticket to make us work on Java 17 - let's not make more work for ourselves!
    private static String typeNameToPartialMethodName(final TypeName originalTypeName) {
        if (originalTypeName.isAnnotated()) {
            return typeNameToPartialMethodName(originalTypeName.withoutAnnotations());
        }

        if (originalTypeName instanceof final ClassName cn) {
            return cn.simpleName();
        } else if (originalTypeName instanceof final ParameterizedTypeName ptn) {
            final String simple = ptn.rawType.simpleName();
            final StringBuilder kinds = new StringBuilder();
            ptn.typeArguments.forEach(t -> kinds.append(typeNameToPartialMethodName(t)));
            return simple + kinds;
        } else if (originalTypeName instanceof final ArrayTypeName atn) {
            return typeNameToPartialMethodName(atn.componentType) + "Arr";
        } else {
            return originalTypeName.withoutAnnotations().toString();
        }
    }
}
