package io.github.cbarlin.aru.tests.c_hkj;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;

@AdvancedRecordUtils(addHigherKindedJImportAnnotation = true)
public sealed interface SealedInterfaceTest permits SealedInterfaceImplA, SealedInterfaceImplB {

}
