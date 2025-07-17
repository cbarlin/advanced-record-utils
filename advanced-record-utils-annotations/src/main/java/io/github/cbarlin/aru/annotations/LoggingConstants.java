package io.github.cbarlin.aru.annotations;

public final class LoggingConstants {
    private LoggingConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String PROCESSOR_NAME_KEY = "advancedRecordUtilsProcessor";
    public static final String PROCESSOR_NAME_VALUE = "io.github.cbarlin.aru.core.AdvRecUtilsProcessor";

    public static final String VISITOR_NAME_KEY = "advancedRecordUtilsVisitor";
    
    public static final String CLAIM_OP_NAME_KEY = "claimedOperationName";

    public static final String CLAIM_OP_TYPE_KEY = "claimedOperationType";
    public static final String CLAIM_OP_TYPE_VALUE_CLASS = "Class";
    public static final String CLAIM_OP_TYPE_VALUE_COMPONENT = "Component";

    public static final String INTENDED_TYPE_KEY = "intendedType";

    public static final String ORIGINAL_TYPE_KEY = "originalType";
}
