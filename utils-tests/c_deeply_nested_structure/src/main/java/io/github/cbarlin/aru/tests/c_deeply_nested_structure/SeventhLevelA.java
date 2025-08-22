package io.github.cbarlin.aru.tests.c_deeply_nested_structure;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import jakarta.xml.bind.annotation.XmlElement;

@AdvancedRecordUtils(
    builderOptions = @AdvancedRecordUtils.BuilderOptions(
        setToNullMethods = true
    )
)
public record SeventhLevelA(
    @XmlElement
    RecurringReference andImDone
) {

}
