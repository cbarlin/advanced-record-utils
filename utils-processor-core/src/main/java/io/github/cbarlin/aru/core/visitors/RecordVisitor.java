package io.github.cbarlin.aru.core.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.LoggingGeneration;
import io.github.cbarlin.aru.core.AdvRecUtilsProcessor;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.CommonsConstants.InternalReferenceNames;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.OperationType;

import io.avaje.spi.Service;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;

/**
 * A visitor that will "go down" a record.
 * <p>
 * Implementaitons will be constructed each time, so they can populate their own fields as they navigate.
 * <p>
 * Each visitor can say that it performs a specific action. If an action that a visitor says it does has already been done,
 *   that visitor won't be called again. Order is determined by {@link #specificity()}
 * <p>
 * The order of operations done by the processor is for each record:
 * <ol>
 *   <li>Construct new instances of all visitors</li>
 *   <li>Prune all instances that return false in their {@link #isApplicable(AnalysedRecord)}</li>
 *   <li>Sort them so that more specific elements are first up</li>
 *   <li>Call the start, per-component, and then end methods. It keeps track of which {@link ClaimableOperation}s have been claimed. 
 *       If the visitor would make the same claim, it is skipped</li>
 * </ol>
 */
@Service
public abstract class RecordVisitor implements Comparable<RecordVisitor> {

    protected final ClaimableOperation claimableOperation;
    private LoggingGeneration loggingGeneration;
    private ClassName targetClassName;
    private ClassName originalClassName;

    protected RecordVisitor(final ClaimableOperation claimableOperation) {
        this.claimableOperation = claimableOperation;
    }

    /**
     * The specificity of the Visitor. More specific visitors are more picky about which classes/fields they 
     *   interact with and thus get first pick of claims, leaving the "catch-all" versions for the end.
     * <p>
     * A reasonable way to determine this value would be (number of evaluations made in {@link #isApplicable(AnalysedRecord)}) * (number of evaluations made when checking class/element)
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
     * @param analysedRecord The record we are about to "walk" down
     * @return True if we should walk it, otherwise false
     */
    public abstract boolean isApplicable(final AnalysedRecord analysedRecord);

    /**
     * Start parsing the class. Implementers should:
     * <ol>
     *  <li>Return true if they performed a class-wide action and actually modified content<li>
     *  <li>Return true if they need to claim the processing of this record, but perform operations at the end (e.g. accumulate all field names first before modifying)</li>
     *  <li>Return false in any other situation (or not implement it)<li>
     * </ol>
     * <p>
     * Implementers should be aware that if this item was claimed by a more specific visitor with the same {@link ClaimableOperation} then this call will be skipped
     * 
     * @param analysedRecord The record we are currently processing
     * @return true if the visitor is claiming the item
     */
    protected boolean visitStartOfClassImpl(final AnalysedRecord analysedRecord) {
        return false;
    }

    /**
     * Visit an element within the class. Implementers should:
     * <ol>
     *  <li>Make any checks necessary for the element in question (e.g. checking annotations, types, etc)</li>
     *  <li>Return true if they have modified something about the state externally to the visitor (e.g. added a method/field/class)</li>
     *  <li>Return false (default) if they did nothing or only changed some state within the Visitor (e.g. add a name to an internal list of names)</li>
     * </ol>
     * <p>
     * Implementers should be aware that if this item was claimed by a more specific visitor with the same {@link ClaimableOperation} then this call will be skipped
     * 
     * @param analysedComponent The component of the record we are currently processing
     */
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        return false;
    }

    /**
     * Let the Visitor know that all fields have been shown to it. Only called if either:
     * <ul>
     *  <li>The visitor doesn't make a class-wide claim</li>
     *  <li>The visitor does make a class-wide claim, and it is the claimant</li>
     * </ul>
     * 
     * @param analysedRecord The 
     */
    protected void visitEndOfClassImpl(final AnalysedRecord analysedRecord) {
        // No-op
    }


    /**
     * Externally callable visiting method. If the operation this visitor aims to do has already been done, it
     *  recluses itself from performing any operations.
     * <p>
     * If it ended up "claiming" the class (i.e. {@link #visitStartOfClassImpl(AnalysedRecord)} returned true), then it will 
     *  mark itself as the claimant.
     * <p>
     * If no class-level claims are made by the visitor, or if it hasn't been claimed yet, the visitor's start class method is invoked (and, by default, does nothing)
     * 
     * @param analysedRecord The record being processed currently
     * @see #visitStartOfClassImpl(AnalysedRecord)
     */
    public final void visitStartOfClass(final AnalysedRecord analysedRecord) {
        final String generation = analysedRecord.settings().prism().logGeneration();
        this.loggingGeneration = LoggingGeneration.valueOf(StringUtils.isBlank(generation) ? "NONE" : generation);
        this.targetClassName = analysedRecord.intendedType();
        this.originalClassName = analysedRecord.className();
        if (analysedRecord.attemptToClaim(this)) {
            final boolean claiming = visitStartOfClassImpl(analysedRecord);
            if (!claiming) {
                analysedRecord.retractClaim(this);
            }
        }
    }

    /**
     * Externally callable visiting method for each component within a record. If the operation this visitor aims to do has already been done, it
     *  recluses itself from performing any operations. It also isn't recluses itself if the visitor makes a per-class claim and it isn't the claimant.
     * <p>
     * If it ended up "claiming" the element (i.e. {@link #visitComponentImpl(AnalysedComponent)} returned true), then it will mark the operation as being performed
     * <p>
     * If no component-level claims are made by the visitor, or if it hasn't been claimed yet, the visitor's per-element method is invoked (and, by default, does nothing)
     * 
     * @see #visitComponentImpl(AnalysedComponent)
     * @param analysedComponent The component currently being processed
     */
    public final void visitComponent(final AnalysedComponent analysedComponent) {
        if (OperationType.CLASS.equals(claimableOperation.operationType()) && (!analysedComponent.parentRecord().attemptToClaim(this))) {
            // We don't hold the claim here
            return;
        }
        if (analysedComponent.attemptToClaim(this)) {
            final boolean claiming = visitComponentImpl(analysedComponent);
            if (!claiming) {
                analysedComponent.retractClaim(this);
            }
        }
    }

    /**
     * Externally callable visit method denoting all fields have been shown to the visitor. Like the other methods, 
     *  the visitor checks to ensure it either doesn't claim anything class-wide, or that it is the claimant first.
     * 
     * @see #visitEndOfClassImpl(AnalysedRecord)
     * @param analysedRecord The record being processed
     */
    public final void visitEndOfClass(final AnalysedRecord analysedRecord) {
        if (analysedRecord.attemptToClaim(this)) {
            visitEndOfClassImpl(analysedRecord);
        }
    }

    private static final String BUILD = "build";

    /**
     * Compare two instances of the record builder
     */
    public final int compareTo(RecordVisitor that) {
        if (claimableOperation.operationName().startsWith(BUILD) && !that.claimableOperation.operationName().startsWith(BUILD)) {
            // prefer me - but remember these are inverted!
            return 1;
        }
        if ((!claimableOperation.operationName().startsWith(BUILD)) && that.claimableOperation.operationName().startsWith(BUILD)) {
            // prefer them - but remember these are inverted!
            return -1;
        }

        if(specificity() == that.specificity()) {
            // Claimable operations are compared instead
            return Integer.compare(hashCode(), that.hashCode());
        }
        return Integer.compare(specificity(), that.specificity());
    }

    @Override
    public final boolean equals(Object obj) {
        return Objects.nonNull(obj) && this.getClass().getCanonicalName().equals(obj.getClass().getCanonicalName());
    }

    @Override
    public final int hashCode() {
        return this.claimableOperation.hashCode();
    }

    /**
     * The operation claimed by this record visitor
     */
    public final ClaimableOperation claimableOperation() {
        return this.claimableOperation;
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
