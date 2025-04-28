package io.github.cbarlin.aru.tests.d_eclipse_collections;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.tests.a_core_dependency.MyRecordA;

@AdvancedRecordUtils
public record DependsOnRecord(
    ImmutableList<MyRecordA> immutableListOfA,
    MutableList<MyRecordA> mutableListOfA,
    ImmutableSet<MyRecordA> immutableSetOfA
) {

}
