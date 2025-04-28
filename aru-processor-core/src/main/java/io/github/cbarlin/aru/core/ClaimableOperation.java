package io.github.cbarlin.aru.core;

import io.github.cbarlin.aru.core.types.OperationType;

public record ClaimableOperation(
    String operationName,
    OperationType operationType
) {

}
