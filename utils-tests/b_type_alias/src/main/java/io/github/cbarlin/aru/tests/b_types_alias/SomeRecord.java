package io.github.cbarlin.aru.tests.b_types_alias;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@AdvancedRecordUtils(merger = true, wither = true, xmlable = true)
@XmlType(name = "SomeTest")
public record SomeRecord(
    @XmlElement(name = "BookName")
    BookName bookName,
    @XmlAttribute(name = "AuthorName")
    AuthorName authorName,
    @XmlAttribute(name = "one")
    RandomIntA randomIntA,
    @XmlAttribute(name = "B")
    RandomIntB randomIntB,
    @XmlAttribute(name = "C")
    RandomIntC randomIntC
) implements SomeRecordUtils.All {

}
