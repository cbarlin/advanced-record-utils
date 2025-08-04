package io.github.cbarlin.aru.core.visitors;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.ARU_LOGGING_CONSTANTS;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.LoggingGeneration;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.CommonsConstants.InternalReferenceNames;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.AnalysedType;
import io.github.cbarlin.aru.core.types.OperationType;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.micronaut.sourcegen.javapoet.ArrayTypeName;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

public abstract class AruVisitor<T extends AnalysedType> implements Comparable<AruVisitor<T>> {
    protected final ClaimableOperation claimableOperation;
    protected final T visitingTarget;
    protected final AdvRecUtilsSettings settings;
    protected final AdvancedRecordUtilsPrism utilsPrism;
    private final LoggingGeneration loggingGeneration;
    private final ClassName targetClassName;
    private final ClassName originalClassName;

    protected AruVisitor(
        final ClaimableOperation claimableOperation, 
        final T target
    ) {
        this.claimableOperation = claimableOperation;
        this.visitingTarget = target;
        this.settings = target.settings();
        this.utilsPrism = target.prism();
        final String generation = utilsPrism.logGeneration();
        this.loggingGeneration = LoggingGeneration.valueOf(StringUtils.isBlank(generation) ? "NONE" : generation);
        this.originalClassName = target.className();
        if (target instanceof final AnalysedRecord analysedRecord) {
            this.targetClassName = analysedRecord.intendedType();
        } else {
            this.targetClassName = originalClassName;
        }
    }

    /**
     * The specificity of the Visitor. More specific visitors are more picky about which classes/fields they 
     *   interact with and thus get first pick of claims, leaving the "catch-all" versions for the end.
     * <p>
     * A reasonable way to determine this value would be the number of evaluations made when checking class/element
     * <p>
     * This allows options that do "fancy" things to be done before more basic ones (e.g. an "only allow setting builder field once" is more specific than "allow setting always")
     * 
     * @return How picky the visitors are. They'll be applied from most to least picky
     */
    public abstract int specificity();

    /**
     * The operation claimed by this record visitor
     */
    public final ClaimableOperation claimableOperation() {
        return this.claimableOperation;
    }

    private static final String BUILD = "build";

    /**
     * Compare two instances of the record builder.
     * <p>
     * Note that it is intentional that this does not match equals/hashCode, as 
     *   we want items to be ordered by their operation and specificity first and foremost.
     * <p>
     * This does <em>not</em> violate the contract even for e.g. SortedSet, since the claim 
     *   and specificity must be constant for a given implementation.
     */
    public final int compareTo(final AruVisitor<T> that) {
        if (claimableOperation.operationName().startsWith(BUILD) && !that.claimableOperation.operationName().startsWith(BUILD)) {
            return -1;
        }
        if ((!claimableOperation.operationName().startsWith(BUILD)) && that.claimableOperation.operationName().startsWith(BUILD)) {
            return 1;
        }

        if(specificity() == that.specificity()) {
            // Canonical names are compared instead
            return this.getClass().getCanonicalName().compareTo(that.getClass().getCanonicalName());
        }
        return Integer.compare(that.specificity(), specificity());
    }

    @Override
    public final boolean equals(final Object obj) {
        // Only include canonicalName since the claimableOperation and specificity are constant for a given implementation
        return Objects.nonNull(obj) && this.getClass().getCanonicalName().equals(obj.getClass().getCanonicalName());
    }

    @Override
    public final int hashCode() {
        // Only include canonicalName since the claimableOperation and specificity are constant for a given implementation
        return new HashCodeBuilder(17, 37)
            .append(this.getClass().getCanonicalName())
            .build();
    }

    /**
     * Converts a TypeName to a partial method name suitable for generating consistent
     * method names across different visitors. Handles nested generics, arrays, and 
     * strips annotations.
     * 
     * @param originalTypeName the TypeName to convert
     * @return a string suitable for use in method names
     */
    @SuppressWarnings({"java:S6880"}) // There is a ticket to make us work on Java 17 - let's not make more work for ourselves!
    protected static String typeNameToPartialMethodName(final TypeName originalTypeName) {
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
            return originalTypeName.toString();
        }
    }

    protected static String capitalise(final String variableName) {
        return (variableName.length() < 2) ? variableName.toUpperCase(Locale.ROOT)
                : (Character.toUpperCase(variableName.charAt(0)) + variableName.substring(1));
    }

    private static final String ADD_KEY_VALUE_TL_STRING = ".addKeyValue($T.$L, $S)";
    private static final String ADD_KEY_VALUE_TL_TL = ".addKeyValue($T.$L, $T.$L)";
    
    private final void logInternal(
        final MethodSpec.Builder methodBuilder,
        final String withinLogCallCode, 
        final List<Object> withinLogCallParams,
        final String atInstruction
    ) {
        if (Objects.nonNull(loggingGeneration) && loggingGeneration != LoggingGeneration.NONE) {
            final List<Object> params = new ArrayList<>();
            final StringBuilder sb = new StringBuilder();
            // Don't bother putting these on new lines - while it does create a really really long generated line...
            //   who is going to read the entire log line of a generated method? Most will just see "__LOGGER.atTrace()"
            //   and move on with life...
            sb.append("$L.$L()");
            params.add(InternalReferenceNames.LOGGER_NAME);
            params.add(atInstruction);

            sb.append(ADD_KEY_VALUE_TL_STRING);
            params.add(ARU_LOGGING_CONSTANTS);
            params.add("VISITOR_NAME_KEY");
            params.add(this.getClass().getCanonicalName());

            sb.append(ADD_KEY_VALUE_TL_TL);
            params.add(ARU_LOGGING_CONSTANTS);
            params.add("PROCESSOR_NAME_KEY");
            params.add(ARU_LOGGING_CONSTANTS);
            params.add("PROCESSOR_NAME_VALUE");

            sb.append(ADD_KEY_VALUE_TL_STRING);
            params.add(ARU_LOGGING_CONSTANTS);
            params.add("CLAIM_OP_NAME_KEY");
            params.add(claimableOperation().operationName());

            sb.append(ADD_KEY_VALUE_TL_TL);
            params.add(ARU_LOGGING_CONSTANTS);
            params.add("CLAIM_OP_TYPE_KEY");
            params.add(ARU_LOGGING_CONSTANTS);
            params.add(OperationType.CLASS.equals(claimableOperation().operationType()) ? "CLAIM_OP_TYPE_VALUE_CLASS" : "CLAIM_OP_TYPE_VALUE_COMPONENT");

            sb.append(ADD_KEY_VALUE_TL_STRING);
            params.add(ARU_LOGGING_CONSTANTS);
            params.add("INTENDED_TYPE_KEY");
            params.add(targetClassName.canonicalName());

            sb.append(ADD_KEY_VALUE_TL_STRING);
            params.add(ARU_LOGGING_CONSTANTS);
            params.add("ORIGINAL_TYPE_KEY");
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
