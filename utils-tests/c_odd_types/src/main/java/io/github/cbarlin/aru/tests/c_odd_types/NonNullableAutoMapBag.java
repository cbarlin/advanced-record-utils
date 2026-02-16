package io.github.cbarlin.aru.tests.c_odd_types;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@AdvancedRecordUtils(
        builderOptions = @AdvancedRecordUtils.BuilderOptions(
                builtCollectionType = AdvancedRecordUtils.BuiltCollectionType.AUTO
                // These should be the defaults:
                // buildNullCollectionToEmpty = true
                // createAdderMethods = true
        ),
        diffable = true,
        diffOptions = @AdvancedRecordUtils.DiffOptions(
                staticMethodsAddedToUtils = true
        ),
        xmlable = true,
        xmlOptions = @AdvancedRecordUtils.XmlOptions(
                inferXmlElementName = AdvancedRecordUtils.NameGeneration.MATCH
        ),
        merger = true,
        mergerOptions = @AdvancedRecordUtils.MergerOptions(
                staticMethodsAddedToUtils = true
        ),
        wither = true
)
public record NonNullableAutoMapBag(
    Map<String, String> stringStringMap,
    Map<String, AnEnum> stringAnEnumMap,
    Map<MapKeyRecord, MapValueRecord> recordMap,
    //
    HashMap<String, String> hashMap,
    TreeMap<String, String> treeMap
) {
}
