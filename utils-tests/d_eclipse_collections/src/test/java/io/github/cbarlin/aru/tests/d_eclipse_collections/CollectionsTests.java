package io.github.cbarlin.aru.tests.d_eclipse_collections;

import io.github.cbarlin.aru.tests.a_core_dependency.AnEnumInDep;
import io.github.cbarlin.aru.tests.b_infer_xml.InferMatchingNameUtils;
import io.github.cbarlin.aru.tests.xml_util.ConvertToXml;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CollectionsTests {

    @Test
    void nonNullImmutableTest() {
        final NonNullableImmutableCollectionBag a = NonNullableImmutableCollectionBagUtils.builder()
                .build();
        assertNotNull(a.immutableSetOfString());
        assertNotNull(a.mutableBooleanList());
        assertNotNull(a.immutableCharSet());
        assertNotNull(a.immutableIntList());
        final NonNullableImmutableCollectionBag b = NonNullableImmutableCollectionBagUtils.builder()
                .immutableBooleanList(null)
                .build();
        assertNotNull(b.immutableBooleanList());

        final NonNullableImmutableCollectionBag c = NonNullableImmutableCollectionBagUtils.builder()
                // "remove" working on empty collections...
                .removeImmutableListOfEnum(AnEnumInDep.MONDAY)
                .addImmutableListOfEnum(AnEnumInDep.TUESDAY)
                .removeImmutableListOfString("A")
                .addImmutableListOfString("B")
                .removeImmutableListOfRecord(InferMatchingNameUtils.builder().build())
                .addImmutableListOfRecord(bu -> bu.iShouldBeAnElement("A"))
                .removeImmutableSetOfEnum(AnEnumInDep.MONDAY)
                .addImmutableSetOfEnum(AnEnumInDep.TUESDAY)
                .removeImmutableSetOfString("A")
                .addImmutableSetOfString("B")
                .removeImmutableSetOfRecord(InferMatchingNameUtils.builder().build())
                .addImmutableSetOfRecord(bu -> bu.iShouldBeAnElement("A"))
                .removeMutableDoubleList(3.0)
                .addMutableDoubleList(5.0)
                .build();
        assertThat(c.immutableListOfEnum())
                .singleElement()
                .isEqualTo(AnEnumInDep.TUESDAY);
        assertThat(c.immutableSetOfString())
                .singleElement()
                .isEqualTo("B");

        // Wither
        final NonNullableImmutableCollectionBag d = c.with().build();
        assertEquals(d, c);
        final NonNullableImmutableCollectionBag e = NonNullableImmutableCollectionBagUtils.builder()
                .addImmutableSetOfEnum(AnEnumInDep.TUESDAY)
                .build();
        // Merge
        final NonNullableImmutableCollectionBag f = d.merge(e);
        assertThat(f.immutableSetOfEnum())
                .singleElement()
                .isEqualTo(AnEnumInDep.TUESDAY);
        // XML
        ConvertToXml.compareXml(out -> assertDoesNotThrow(() -> f.writeSelfTo(out)), "NonNullableImmutableCollectionA.xml");
        final List<AnEnumInDep> enums = List.of(AnEnumInDep.MONDAY, AnEnumInDep.TUESDAY);
        final NonNullableImmutableCollectionBag g = f.with()
                .addImmutableSetOfString("B")
                .addImmutableSetOfString("B")
                .addImmutableListOfEnum(enums)
                .addImmutableListOfEnum(enums)
                .addImmutableListOfString("A")
                .addImmutableListOfString("A")
                .addImmutableListOfString("A")
                .addImmutableListOfString("B")
                .addImmutableListOfString("C")
                .addImmutableListOfRecord(bu -> bu.iShouldBeAnElement("B"))
                .addImmutableListOfRecord(bu -> bu.iShouldBeAnElement("C"))
                .addMutableSetOfString("B")
                .addMutableSetOfString("B")
                .addMutableListOfEnum(enums)
                .addMutableListOfEnum(enums)
                .addMutableListOfString("A")
                .addMutableListOfString("A")
                .addMutableListOfString("A")
                .addMutableListOfString("B")
                .addMutableListOfString("C")
                .addMutableListOfRecord(bu -> bu.iShouldBeAnElement("B"))
                .addMutableListOfRecord(bu -> bu.iShouldBeAnElement("C"))
                .addImmutableBooleanList(true)
                .addImmutableBooleanList(true)
                .addImmutableBooleanList(true)
                .addMutableBooleanList(false)
                .addMutableBooleanList(true)
                .addImmutableBooleanSet(true)
                .addImmutableBooleanSet(true)
                .addImmutableBooleanSet(true)
                .addMutableBooleanSet(false)
                .addMutableBooleanSet(true)
                .build();
        ConvertToXml.compareXml(out -> assertDoesNotThrow(() -> g.writeSelfTo(out)), "NonNullableImmutableCollectionB.xml");
    }

}
