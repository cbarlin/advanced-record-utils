package io.github.cbarlin.aru.tests.d_fastutils;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.LoggingGeneration;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.NameGeneration;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.XmlOptions;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.booleans.BooleanSet;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.bytes.ByteSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortSet;

@AdvancedRecordUtils(
    xmlable = true,
    wither = true,
    merger = true,
    logGeneration = LoggingGeneration.SLF4J_GENERATED_UTIL_INTERFACE,
    createAllInterface = true,
    xmlOptions = @XmlOptions(
        inferXmlElementName = NameGeneration.UPPER_FIRST_LETTER
    )
)
public record FastUtilCollections(
    ShortList shortList,
    ShortSet shortSet,
    BooleanList booleanList,
    BooleanSet booleanSet,
    ByteList byteList,
    ByteSet byteSet
) {

}
