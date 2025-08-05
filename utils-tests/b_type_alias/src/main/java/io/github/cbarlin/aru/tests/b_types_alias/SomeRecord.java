package io.github.cbarlin.aru.tests.b_types_alias;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.tests.a_core_dependency.AnEnumInDep;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
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
    RandomIntC randomIntC,
    @XmlTransient
    AnEnumInDep anEnumInDep,
    @XmlTransient
    String another
) implements SomeRecordUtils.All {

    // Test to ensure that inferring intended constructor without annotation works
    public SomeRecord (
        BookName bookName,
        AuthorName authorName,
        RandomIntA randomIntA,
        RandomIntB randomIntB,
        RandomIntC randomIntC,
        AnEnumInDep anEnumInDep
    ) {
        this(bookName, authorName, randomIntA, randomIntB, randomIntC, anEnumInDep, "Some string");
    }
}
