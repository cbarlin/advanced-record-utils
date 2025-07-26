package io.github.cbarlin.aru.tests.d_eclipse_collections;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.tests.a_core_dependency.MyRecordA;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;

@AdvancedRecordUtils(attemptToFindExistingUtils = true)
public record DependsOnRecord(
    ImmutableList<MyRecordA> immutableListOfA,
    MutableList<MyRecordA> mutableListOfA,
    ImmutableSet<MyRecordA> immutableSetOfA,
    // Check to make sure that nested parameterised types works
    ImmutableList<ImmutableList<ImmutableList<MyRecordA>>> someSillyNestedStructure
) {
}
