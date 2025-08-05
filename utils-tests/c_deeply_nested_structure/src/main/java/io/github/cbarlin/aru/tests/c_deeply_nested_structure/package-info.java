@jakarta.xml.bind.annotation.XmlSchema(
    namespace = "ns://default",
    xmlns = {
        @XmlNs(namespaceURI = "ns://namedA", prefix = "wooo"),
        @XmlNs(namespaceURI = "ns://namedB", prefix = "yayyyyy")
    }
)
@AdvancedRecordUtils(
    builderOptions = @AdvancedRecordUtils.BuilderOptions(
        buildMethodName = "make",
        setTimeNowMethodPrefix = "update"
    )
)
package io.github.cbarlin.aru.tests.c_deeply_nested_structure;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import jakarta.xml.bind.annotation.XmlNs;
