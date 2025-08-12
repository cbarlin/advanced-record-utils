@jakarta.xml.bind.annotation.XmlSchema(
    namespace = "ns://default",
    xmlns = {
        @XmlNs(namespaceURI = "ns://namedA", prefix = "wooo"),
        @XmlNs(namespaceURI = "ns://namedB", prefix = "yayyyyy")
    }
)
package io.github.cbarlin.aru.tests.cdncl;

import jakarta.xml.bind.annotation.XmlNs;
