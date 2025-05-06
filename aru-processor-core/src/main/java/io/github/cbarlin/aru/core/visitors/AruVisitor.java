package io.github.cbarlin.aru.core.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.LoggingGeneration;
import io.github.cbarlin.aru.core.AdvRecUtilsProcessor;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.CommonsConstants.InternalReferenceNames;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.AnalysedType;
import io.github.cbarlin.aru.core.types.OperationType;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;

public abstract class AruVisitor<T extends AnalysedType> implements Comparable<AruVisitor<T>> {
    protected final ClaimableOperation claimableOperation;
    private LoggingGeneration loggingGeneration;
    private ClassName targetClassName;
    private ClassName originalClassName;

    protected AruVisitor(final ClaimableOperation claimableOperation) {
        this.claimableOperation = claimableOperation;
    }

    /**
     * The specificity of the Visitor. More specific visitors are more picky about which classes/fields they 
     *   interact with and thus get first pick of claims, leaving the "catch-all" versions for the end.
     * <p>
     * A reasonable way to determine this value would be (number of evaluations made in {@link #isApplicable(T)}) * (number of evaluations made when checking class/element)
     * <p>
     * This allows options that do "fancy" things to be done before more basic ones (e.g. an "only allow setting builder field once" is more specific than "allow setting always")
     * 
     * @return How picky the visitors are. They'll be applied from most to least picky
     */
    public abstract int specificity();

    /**
     * Determine if this visitor can visit this class. implementers should:
     * <ol>
     *  <li>Make any evaluations regarding if the settings/annotations/etc include or exclude this visitor</li>
     *  <li>Perform any initialisation (e.g. making a local field a builder) they need to do</li>
     * </ol>
     * 
     * @param target The record we are about to "walk" down
     * @return True if we should walk it, otherwise false
     */
    public abstract boolean isApplicable(final T target);

    public final void configureLogging(final T target) {
        final String generation = target.settings().prism().logGeneration();
        this.loggingGeneration = LoggingGeneration.valueOf(StringUtils.isBlank(generation) ? "NONE" : generation);
        this.originalClassName = target.className();
        if (target instanceof final AnalysedRecord analysedRecord) {
            this.targetClassName = analysedRecord.intendedType();
        } else {
            this.targetClassName = originalClassName;
        }
    }

    /**
     * The operation claimed by this record visitor
     */
    public final ClaimableOperation claimableOperation() {
        return this.claimableOperation;
    }

    private static final String BUILD = "build";

    /**
     * Compare two instances of the record builder
     */
    public final int compareTo(final AruVisitor<T> that) {
        if (claimableOperation.operationName().startsWith(BUILD) && !that.claimableOperation.operationName().startsWith(BUILD)) {
            // prefer me - but remember these are inverted!
            return 1;
        }
        if ((!claimableOperation.operationName().startsWith(BUILD)) && that.claimableOperation.operationName().startsWith(BUILD)) {
            // prefer them - but remember these are inverted!
            return -1;
        }

        if(specificity() == that.specificity()) {
            // Canonical names are compared instead
            return Integer.compare(hashCode(), that.hashCode());
        }
        return Integer.compare(specificity(), that.specificity());
    }

    @Override
    public final boolean equals(final Object obj) {
        return Objects.nonNull(obj) && this.getClass().getCanonicalName().equals(obj.getClass().getCanonicalName());
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getClass().getCanonicalName())
            .build();
    }

    private static final String ADD_KEY_VALUE_STRING_STRING = ".addKeyValue($S, $S)";
    
    private final void logInternal(
        final MethodSpec.Builder methodBuilder,
        final String withinLogCallCode, 
        final List<Object> withinLogCallParams,
        final String atInstruction
    ) {
        if (loggingGeneration != LoggingGeneration.NONE) {
            final List<Object> params = new ArrayList<>();
            final StringBuilder sb = new StringBuilder();
            // Don't bother putting these on new lines - while it does create a really really long generated line...
            //   who is going to read the entire log line of a generated method? Most will just see "__LOGGER.atTrace()"
            //   and move on with life...
            sb.append("$L.$L()");
            params.add(InternalReferenceNames.LOGGER_NAME);
            params.add(atInstruction);

            sb.append(ADD_KEY_VALUE_STRING_STRING);
            params.add("advancedRecordUtilsVisitor");
            params.add(this.getClass().getCanonicalName());

            sb.append(ADD_KEY_VALUE_STRING_STRING);
            params.add("advancedRecordUtilsProcessor");
            params.add(AdvRecUtilsProcessor.class.getCanonicalName());

            sb.append(ADD_KEY_VALUE_STRING_STRING);
            params.add("claimedOperationName");
            params.add(claimableOperation().operationName());

            sb.append(ADD_KEY_VALUE_STRING_STRING);
            params.add("claimedOperationType");
            params.add(OperationType.CLASS.equals(claimableOperation().operationType()) ? "Class" : "Component");

            sb.append(ADD_KEY_VALUE_STRING_STRING);
            params.add("intendedType");
            params.add(targetClassName.canonicalName());

            sb.append(ADD_KEY_VALUE_STRING_STRING);
            params.add("originalType");
            params.add(originalClassName.canonicalName());

            sb.append(".log(").append(withinLogCallCode).append(")");
            params.addAll(withinLogCallParams);
            methodBuilder.addStatement(sb.toString(), params.toArray());
        } else {
            methodBuilder.addComment(withinLogCallCode, withinLogCallParams.toArray());
        }
    }

    /**
     * Add a log line (or comment, if logging is disabled) to the method
     * 
     * @param methodBuilder The method to add the log (or comment) to
     * @param withinLogFormat The JavaPoet format to use in the `addStatement` of the `log(...)` call
     * @param withinLogArgs The JavaPoet args to use in the `addStatement` of the `log(...)` call
     */
    public final void logTrace(
        final MethodSpec.Builder methodBuilder, 
        final String withinLogFormat, 
        final List<Object> withinLogArgs 
    ) {
        logInternal(methodBuilder, withinLogFormat, withinLogArgs, "atTrace");
    }

    /**
     * Add a log line (or comment, if logging is disabled) to the method
     * 
     * @param methodBuilder The method to add the log (or comment) to
     * @param simpleLine The string to log
     */
    public final void logTrace(
        final MethodSpec.Builder methodBuilder,
        final String simpleLine
    ) {
        this.logTrace(methodBuilder, "$S", List.of(simpleLine));
    }

    /**
     * Add a log line (or comment, if logging is disabled) to the method
     * 
     * @param methodBuilder The method to add the log (or comment) to
     * @param withinLogFormat The JavaPoet format to use in the `addStatement` of the `log(...)` call
     * @param withinLogArgs The JavaPoet args to use in the `addStatement` of the `log(...)` call
     */
    public final void logDebug(
        final MethodSpec.Builder methodBuilder,
        final String withinLogFormat, 
        final List<Object> withinLogArgs 
    ) {
        logInternal(methodBuilder, withinLogFormat, withinLogArgs, "atDebug");
    }

    /**
     * Add a log line (or comment, if logging is disabled) to the method
     * 
     * @param methodBuilder The method to add the log (or comment) to
     * @param simpleLine The string to log
     */
    public final void logDebug(
        final MethodSpec.Builder methodBuilder,
        final String simpleLine
    ) {
        this.logDebug(methodBuilder, "$S", List.of(simpleLine));
    }

    /**
     * Add a log line (or comment, if logging is disabled) to the method
     * 
     * @param methodBuilder The method to add the log (or comment) to
     * @param withinLogFormat The JavaPoet format to use in the `addStatement` of the `log(...)` call
     * @param withinLogArgs The JavaPoet args to use in the `addStatement` of the `log(...)` call
     */
    public final void logInfo(
        final MethodSpec.Builder methodBuilder,
        final String withinLogFormat, 
        final List<Object> withinLogArgs 
    ) {
        logInternal(methodBuilder, withinLogFormat, withinLogArgs, "atInfo");
    }

    /**
     * Add a log line (or comment, if logging is disabled) to the method
     * 
     * @param methodBuilder The method to add the log (or comment) to
     * @param simpleLine The string to log
     */
    public final void logInfo(
        final MethodSpec.Builder methodBuilder,
        final String simpleLine
    ) {
        this.logInfo(methodBuilder, "$S", List.of(simpleLine));
    }

    /**
     * Add a log line (or comment, if logging is disabled) to the method
     * 
     * @param methodBuilder The method to add the log (or comment) to
     * @param withinLogFormat The JavaPoet format to use in the `addStatement` of the `log(...)` call
     * @param withinLogArgs The JavaPoet args to use in the `addStatement` of the `log(...)` call
     */
    public final void logWarn(
        final MethodSpec.Builder methodBuilder,
        final String withinLogFormat, 
        final List<Object> withinLogArgs 
    ) {
        logInternal(methodBuilder, withinLogFormat, withinLogArgs, "atWarn");
    }

    /**
     * Add a log line (or comment, if logging is disabled) to the method
     * 
     * @param methodBuilder The method to add the log (or comment) to
     * @param simpleLine The string to log
     */
    public final void logWarn(
        final MethodSpec.Builder methodBuilder,
        final String simpleLine
    ) {
        this.logWarn(methodBuilder, "$S", List.of(simpleLine));
    }
}
