@jakarta.xml.bind.annotation.XmlSchema(
    xmlns = {
        @jakarta.xml.bind.annotation.XmlNs(
            namespaceURI= "ns://Optionals",
            prefix="opt"
        )
    }
)
@io.github.cbarlin.aru.annotations.AdvancedRecordUtils(
    merger = true,
    applyToAllInPackage = true,
    diffable = true
)
package io.github.cbarlin.aru.tests.c_odd_types;
