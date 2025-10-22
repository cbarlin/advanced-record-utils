package io.github.cbarlin.aru.tests.d_eclipse_collections;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuilderOptions;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.NameGeneration;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.XmlOptions;
import io.github.cbarlin.aru.tests.a_core_dependency.AnEnumInDep;
import io.github.cbarlin.aru.tests.b_infer_xml.InferMatchingName;
import io.github.cbarlin.aru.tests.b_infer_xml.InferMatchingNameUtils;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.*;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.primitive.*;

@AdvancedRecordUtils(
    builderOptions = @BuilderOptions(
        // These should both be the defaults
        // builtCollectionType = BuiltCollectionType.JAVA_IMMUTABLE,
        // buildNullCollectionToEmpty = true
    ),
    diffable = true,
    diffOptions = @AdvancedRecordUtils.DiffOptions(
        staticMethodsAddedToUtils = true
    ),
    xmlable = true,
    xmlOptions = @XmlOptions(
        inferXmlElementName = NameGeneration.MATCH
    ),
    merger = true,
    mergerOptions = @AdvancedRecordUtils.MergerOptions(
        staticMethodsAddedToUtils = true
    ),
    wither = true,
    importTargets = {
        InferMatchingNameUtils.class
    }
)
public record NonNullableImmutableCollectionBag(
    // We will do immutable and mutable of enum/string/record
    ImmutableSet<AnEnumInDep> immutableSetOfEnum,
    ImmutableSet<String> immutableSetOfString,
    ImmutableSet<InferMatchingName> immutableSetOfRecord,
    ImmutableList<AnEnumInDep> immutableListOfEnum,
    ImmutableList<String> immutableListOfString,
    ImmutableList<InferMatchingName> immutableListOfRecord,
    MutableSet<AnEnumInDep> mutableSetOfEnum,
    MutableSet<String> mutableSetOfString,
    MutableSet<InferMatchingName> mutableSetOfRecord,
    MutableList<AnEnumInDep> mutableListOfEnum,
    MutableList<String> mutableListOfString,
    MutableList<InferMatchingName> mutableListOfRecord,
    // And, of course, the primitives
    ImmutableByteList immutableByteList,
    MutableByteList mutableByteList,
    ImmutableByteSet immutableByteSet,
    MutableByteSet mutableByteSet,
    ImmutableCharList immutableCharList,
    MutableCharList mutableCharList,
    ImmutableCharSet immutableCharSet,
    MutableCharSet mutableCharSet,
    ImmutableDoubleList immutableDoubleList,
    MutableDoubleList mutableDoubleList,
    ImmutableDoubleSet immutableDoubleSet,
    MutableDoubleSet mutableDoubleSet,
    ImmutableFloatList immutableFloatList,
    MutableFloatList mutableFloatList,
    ImmutableFloatSet immutableFloatSet,
    MutableFloatSet mutableFloatSet,
    ImmutableIntList immutableIntList,
    MutableIntList mutableIntList,
    ImmutableIntSet immutableIntSet,
    MutableIntSet mutableIntSet,
    ImmutableLongList immutableLongList,
    MutableLongList mutableLongList,
    ImmutableLongSet immutableLongSet,
    MutableLongSet mutableLongSet,
    ImmutableShortList immutableShortList,
    MutableShortList mutableShortList,
    ImmutableShortSet immutableShortSet,
    MutableShortSet mutableShortSet,
    ImmutableBooleanList immutableBooleanList,
    MutableBooleanList mutableBooleanList,
    ImmutableBooleanSet immutableBooleanSet,
    MutableBooleanSet mutableBooleanSet
) implements NonNullableImmutableCollectionBagUtils.All {

}
