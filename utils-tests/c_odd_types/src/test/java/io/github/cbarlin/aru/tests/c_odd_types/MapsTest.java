package io.github.cbarlin.aru.tests.c_odd_types;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapsTest {

    @Test
    void detectsSubRecords() {
        final MapKeyRecord a = MapKeyRecordUtils.builder()
                .value(42)
                .build();
        final MapValueRecord b = MapValueRecordUtils.builder()
                .otherValue(42)
                .build();
        assertEquals(a.value(), b.otherValue());
    }

    @Test
    void nonNullImmutable() {
        final NonNullableImmutableMapBag built = NonNullableImmutableMapBagUtils.builder()
                .addStringStringMap("AA", "BB")
                .build();
        assertThat(built.stringStringMap())
                .hasSize(1)
                .containsEntry("AA", "BB");
        assertThat(built.hashMap())
                .isNotNull()
                .isEmpty();
        assertThat(built.treeMap())
                .isNotNull()
                .isEmpty();
        assertThat(built.recordMap())
                .isNotNull()
                .isEmpty();
        assertThat(built.stringAnEnumMap())
                .isNotNull()
                .isEmpty();
        assertThrows(UnsupportedOperationException.class, () -> built.stringStringMap().put("A", "B"));
        assertThrows(UnsupportedOperationException.class, () -> built.stringAnEnumMap().put("A", AnEnum.ONE));
    }

    @Test
    void nonNullAuto() {
        final NonNullableAutoMapBag built = NonNullableAutoMapBagUtils.builder()
                .addStringStringMap("AA", "BB")
                .build();
        assertThat(built.stringStringMap())
                .hasSize(1)
                .containsEntry("AA", "BB");
        assertThat(built.hashMap())
                .isNotNull()
                .isEmpty();
        assertThat(built.treeMap())
                .isNotNull()
                .isEmpty();
        assertThat(built.recordMap())
                .isNotNull()
                .isEmpty();
        assertThat(built.stringAnEnumMap())
                .isNotNull()
                .isEmpty();
        assertDoesNotThrow(() -> built.stringStringMap().put("A", "B"));
        assertThrows(UnsupportedOperationException.class, () -> built.stringAnEnumMap().put("A", AnEnum.ONE));
    }
}
