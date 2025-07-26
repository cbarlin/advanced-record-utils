package io.github.cbarlin.aru.core.visitors;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.OperationType;

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
 *   <li>Prune all instances that return false in their {@code isApplicable}</li>
 *   <li>Sort them so that more specific elements are first up</li>
 *   <li>Call the start, per-component, and then end methods. It keeps track of which {@link ClaimableOperation}s have been claimed. 
 *       If the visitor would make the same claim, it is skipped</li>
 * </ol>
 */
public abstract class RecordVisitor extends AruVisitor<AnalysedRecord> {

    protected final AnalysedRecord analysedRecord;

    protected RecordVisitor(final ClaimableOperation claimableOperation, final AnalysedRecord analysedRecord) {
        super(claimableOperation, analysedRecord);
        this.analysedRecord = analysedRecord;
    }

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
     * @return true if the visitor is claiming the item
     */
    protected boolean visitStartOfClassImpl() {
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
     */
    protected void visitEndOfClassImpl() {
        // No-op
    }

    /**
     * Externally callable visiting method. If the operation this visitor aims to do has already been done, it
     *  recluses itself from performing any operations.
     * <p>
     * If it ended up "claiming" the class (i.e. {@link #visitStartOfClassImpl()} returned true), then it will
     *  mark itself as the claimant.
     * <p>
     * If no class-level claims are made by the visitor, or if it hasn't been claimed yet, the visitor's start class method is invoked (and, by default, does nothing)
     * 
     * @see #visitStartOfClassImpl()
     */
    public final void visitStartOfClass() {
        if (visitingTarget.attemptToClaim(this)) {
            final boolean claiming = visitStartOfClassImpl();
            if (!claiming) {
                visitingTarget.retractClaim(this);
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
     * @see #visitEndOfClassImpl()
     */
    public final void visitEndOfClass() {
        if (visitingTarget.attemptToClaim(this)) {
            visitEndOfClassImpl();
        }
    }    
}
