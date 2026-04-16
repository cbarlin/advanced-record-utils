package io.github.cbarlin.aru.tests.d_eclipse_collections;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.tests.b_infer_xml.InferMatchingNameUtils;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.primitive.ImmutableCharBooleanMap;
import org.eclipse.collections.api.map.primitive.MutableIntShortMap;

@AdvancedRecordUtils(
        builderOptions = @AdvancedRecordUtils.BuilderOptions(
                // These should both be the defaults
                // builtCollectionType = BuiltCollectionType.JAVA_IMMUTABLE,
                // buildNullCollectionToEmpty = true
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
        importTargets = {
                InferMatchingNameUtils.class
        }
)
public record NonNullableImmutableMapBag(
    ImmutableMap<String, String> immutableMap,
    MutableMap<String, String> mutableMap,
    ImmutableCharBooleanMap immutableCharBooleanMap,
    MutableIntShortMap intShortMap
) {
}
