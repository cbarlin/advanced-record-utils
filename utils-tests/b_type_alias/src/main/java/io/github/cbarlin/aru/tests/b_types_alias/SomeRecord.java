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
    String another,
    @XmlElement(name = "vCsL")
    ValueClsLong valueClsLong,
    @XmlElement(name = "vCsInt")
    ValueClsInt valueClsInt,
    @XmlElement(name = "vCsStr")
    ValueClsString valueClsString
) implements SomeRecordUtils.All {

    // Test to ensure that inferring intended constructor without annotation works
    public SomeRecord (
        final BookName bookName,
        final AuthorName authorName,
        final RandomIntA randomIntA,
        final RandomIntB randomIntB,
        final RandomIntC randomIntC,
        final AnEnumInDep anEnumInDep
    ) {
        this(bookName, authorName, randomIntA, randomIntB, randomIntC, anEnumInDep, "Some string", new ValueClsLong(5L), new ValueClsInt(69), new ValueClsString("Can this work?"));
    }
}
