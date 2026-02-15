package io.github.cbarlin.aru.tests.c_hkj;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;

@AdvancedRecordUtils(
    addHigherKindedJImportAnnotation = true
)
public record RootElement(
    Rec2 otherRecord
) {
}
