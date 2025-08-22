package io.github.cbarlin.aru.tests.c_odd_types;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CollectionsTests {

    @Test
    void nonNullImmTest() {
        final NonNullableImmutableCollectionBag a = NonNullableImmutableCollectionBagUtils.builder()
            .build();
        // All of these should not be null
        assertNotNull(a.setOfEnum());
        assertNotNull(a.enumSetOfEnum());
        assertNotNull(a.hashSetOfEnum());
        assertNotNull(a.treeSetOfEnum());
        assertNotNull(a.sortedSetOfEnum());
        assertNotNull(a.setOfString());
        assertNotNull(a.hashSetOfString());
        assertNotNull(a.treeSetOfString());
        assertNotNull(a.sortedSetOfString());
        assertNotNull(a.listOfString());
        assertNotNull(a.arrayListOfString());
        assertNotNull(a.linkedListOfString());
        assertNotNull(a.vectorOfString());
        assertNotNull(a.stackOfString());
        // And the "generic" ("Set", "List") ones should not permit me to add to them
        assertThrows(UnsupportedOperationException.class, () -> a.setOfEnum().add(AnEnum.ONE));
        assertThrows(UnsupportedOperationException.class, () -> a.setOfString().add("A"));
        assertThrows(UnsupportedOperationException.class, () -> a.listOfString().add("A"));

        // Even if I explicitly set one to null, I should get a non-null output
        final NonNullableImmutableCollectionBag b = NonNullableImmutableCollectionBagUtils.builder()
            .setOfEnum(null)
            .build();
        assertNotNull(b.setOfEnum());

        // OK, lets try and do a diff
        final NonNullableImmutableCollectionBag c = NonNullableImmutableCollectionBagUtils.builder()
            .addEnumSetOfEnum(AnEnum.TWO)
            .addHashSetOfEnum(Set.of(AnEnum.ONE, AnEnum.THREE))
            .addTreeSetOfString("This is a string!")
            .addLinkedListOfString(Stream.of("A").iterator())
            .addLinkedListOfString(Stream.of("A").spliterator())
            .addStackOfString(List.of("A"))
            .build();
        final var diff = NonNullableImmutableCollectionBagUtils.diff(b, c);
        assertFalse(diff.hasSetOfEnumChanged(), "setOfEnum");
        assertTrue(diff.hasEnumSetOfEnumChanged(), "enumSetOfEnum");
        assertTrue(diff.hasHashSetOfEnumChanged(), "hashSetOfEnum");
        assertFalse(diff.hasTreeSetOfEnumChanged(), "treeSetOfEnum");
        assertFalse(diff.hasSortedSetOfEnumChanged(), "sortedSetOfEnum");
        assertFalse(diff.hasSetOfStringChanged(), "setOfString");
        assertFalse(diff.hasHashSetOfStringChanged(), "hashSetOfString");
        assertTrue(diff.hasTreeSetOfStringChanged(), "treeSetOfString");
        assertFalse(diff.hasSortedSetOfStringChanged(), "sortedSetOfString");
        assertFalse(diff.hasListOfStringChanged(), "listOfString");
        assertFalse(diff.hasArrayListOfStringChanged(), "arrayListOfString");
        assertTrue(diff.hasLinkedListOfStringChanged(), "linkedListOfString");
        assertFalse(diff.hasVectorOfStringChanged(), "vectorOfString");
        assertTrue(diff.hasStackOfStringChanged(), "stackOfString");

        assertThat(diff.diffEnumSetOfEnum().addedElements())
            .isUnmodifiable()
            .hasSize(1)
            .containsExactly(AnEnum.TWO);

        assertThat(diff.diffEnumSetOfEnum().elementsInCommon())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffEnumSetOfEnum().removedElements())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffVectorOfString().addedElements())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffVectorOfString().elementsInCommon())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffVectorOfString().removedElements())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffStackOfString().addedElements())
            .isUnmodifiable()
            .hasSize(1)
            .containsExactly("A");

        assertThat(diff.diffLinkedListOfString().addedElements())
            .isUnmodifiable()
            .hasSize(2)
            .containsExactly("A", "A");

        // Let's merge two instances together!
        final NonNullableImmutableCollectionBag d = NonNullableImmutableCollectionBagUtils.merge(
            c,
            NonNullableImmutableCollectionBagUtils.builder()
                .addSetOfEnum(AnEnum.ONE)
                .addLinkedListOfString("B")
                .build()
        );

        assertNotNull(d);

        assertThat(d.setOfEnum())
            .isNotNull()
            .isUnmodifiable()
            .hasSize(1)
            .containsExactly(AnEnum.ONE);

        assertThat(d.enumSetOfEnum())
            .isNotNull()
            .hasSameElementsAs(c.enumSetOfEnum());

        assertThat(d.linkedListOfString())
            .isNotNull()
            .hasSize(3)
            .containsExactly("A", "A", "B");
    }

    @Test
    void nonNullAutoTest() {
        final NonNullableAutoCollectionBag a = NonNullableAutoCollectionBagUtils.builder()
            .build();
        // All of these should not be null
        assertNotNull(a.setOfEnum());
        assertNotNull(a.enumSetOfEnum());
        assertNotNull(a.hashSetOfEnum());
        assertNotNull(a.treeSetOfEnum());
        assertNotNull(a.sortedSetOfEnum());
        assertNotNull(a.setOfString());
        assertNotNull(a.hashSetOfString());
        assertNotNull(a.treeSetOfString());
        assertNotNull(a.sortedSetOfString());
        assertNotNull(a.listOfString());
        assertNotNull(a.arrayListOfString());
        assertNotNull(a.linkedListOfString());
        assertNotNull(a.vectorOfString());
        assertNotNull(a.stackOfString());
        // And the "generic" ("Set", "List") ones should not permit me to add to them
        assertThat(a.setOfEnum())
            .isInstanceOf(EnumSet.class);
        assertThat(a.setOfString())
            .isInstanceOf(HashSet.class);
        assertThat(a.listOfString())
            .isInstanceOf(ArrayList.class);

        // Even if I explicitly set one to null, I should get a non-null output
        final NonNullableAutoCollectionBag b = NonNullableAutoCollectionBagUtils.builder()
            .setOfEnum(null)
            .build();
        assertNotNull(b.setOfEnum());

        // OK, lets try and do a diff
        final NonNullableAutoCollectionBag c = NonNullableAutoCollectionBagUtils.builder()
            .addEnumSetOfEnum(AnEnum.TWO)
            .addHashSetOfEnum(Set.of(AnEnum.ONE, AnEnum.THREE))
            .addTreeSetOfString("This is a string!")
            .addLinkedListOfString(Stream.of("A").iterator())
            .addLinkedListOfString(Stream.of("A").spliterator())
            .addStackOfString(List.of("A"))
            .build();
        final var diff = NonNullableAutoCollectionBagUtils.diff(b, c);
        assertFalse(diff.hasSetOfEnumChanged(), "setOfEnum");
        assertTrue(diff.hasEnumSetOfEnumChanged(), "enumSetOfEnum");
        assertTrue(diff.hasHashSetOfEnumChanged(), "hashSetOfEnum");
        assertFalse(diff.hasTreeSetOfEnumChanged(), "treeSetOfEnum");
        assertFalse(diff.hasSortedSetOfEnumChanged(), "sortedSetOfEnum");
        assertFalse(diff.hasSetOfStringChanged(), "setOfString");
        assertFalse(diff.hasHashSetOfStringChanged(), "hashSetOfString");
        assertTrue(diff.hasTreeSetOfStringChanged(), "treeSetOfString");
        assertFalse(diff.hasSortedSetOfStringChanged(), "sortedSetOfString");
        assertFalse(diff.hasListOfStringChanged(), "listOfString");
        assertFalse(diff.hasArrayListOfStringChanged(), "arrayListOfString");
        assertTrue(diff.hasLinkedListOfStringChanged(), "linkedListOfString");
        assertFalse(diff.hasVectorOfStringChanged(), "vectorOfString");
        assertTrue(diff.hasStackOfStringChanged(), "stackOfString");

        assertThat(diff.diffEnumSetOfEnum().addedElements())
            .isUnmodifiable()
            .hasSize(1)
            .containsExactly(AnEnum.TWO);

        assertThat(diff.diffEnumSetOfEnum().elementsInCommon())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffEnumSetOfEnum().removedElements())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffVectorOfString().addedElements())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffVectorOfString().elementsInCommon())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffVectorOfString().removedElements())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffStackOfString().addedElements())
            .isUnmodifiable()
            .hasSize(1)
            .containsExactly("A");

        assertThat(diff.diffLinkedListOfString().addedElements())
            .isUnmodifiable()
            .hasSize(2)
            .containsExactly("A", "A");

        // Let's merge two instances together!
        final NonNullableAutoCollectionBag d = NonNullableAutoCollectionBagUtils.merge(
            c,
            NonNullableAutoCollectionBagUtils.builder()
                .addSetOfEnum(AnEnum.ONE)
                .addLinkedListOfString("B")
                .build()
        );

        assertNotNull(d);

        assertThat(d.setOfEnum())
            .isNotNull()
            .hasSize(1)
            .containsExactly(AnEnum.ONE);

        assertThat(d.enumSetOfEnum())
            .isNotNull()
            .hasSameElementsAs(c.enumSetOfEnum());

        assertThat(d.linkedListOfString())
            .isNotNull()
            .hasSize(3)
            .containsExactly("A", "A", "B");
    }

    @Test
    void nlImmTest() {
        final NullableImmutableCollectionBag a = NullableImmutableCollectionBagUtils.builder()
            .build();
        // All of these should be null!
        assertNull(a.setOfEnum());
        assertNull(a.enumSetOfEnum());
        assertNull(a.hashSetOfEnum());
        assertNull(a.treeSetOfEnum());
        assertNull(a.sortedSetOfEnum());
        assertNull(a.setOfString());
        assertNull(a.hashSetOfString());
        assertNull(a.treeSetOfString());
        assertNull(a.sortedSetOfString());
        assertNull(a.listOfString());
        assertNull(a.arrayListOfString());
        assertNull(a.linkedListOfString());
        assertNull(a.vectorOfString());
        assertNull(a.stackOfString());
        // If I try and add something though, it should be non-modifiable
        final NullableImmutableCollectionBag b = NullableImmutableCollectionBagUtils.builder()
            .addSetOfEnum(AnEnum.ONE)
            .build();
        assertThat(b.setOfEnum())
            .isNotNull()
            .isUnmodifiable()
            .hasSize(1)
            .containsExactly(AnEnum.ONE);

        // OK, lets try and do a diff
        final NullableImmutableCollectionBag c = NullableImmutableCollectionBagUtils.builder()
            .addEnumSetOfEnum(AnEnum.TWO)
            .addHashSetOfEnum(Set.of(AnEnum.ONE, AnEnum.THREE))
            .addTreeSetOfString("This is a string!")
            .addLinkedListOfString(Stream.of("A").iterator())
            .addLinkedListOfString(Stream.of("A").spliterator())
            .addStackOfString(List.of("A"))
            .build();
        final var diff = NullableImmutableCollectionBagUtils.diff(a, c);
        assertFalse(diff.hasSetOfEnumChanged(), "setOfEnum");
        assertTrue(diff.hasEnumSetOfEnumChanged(), "enumSetOfEnum");
        assertTrue(diff.hasHashSetOfEnumChanged(), "hashSetOfEnum");
        assertFalse(diff.hasTreeSetOfEnumChanged(), "treeSetOfEnum");
        assertFalse(diff.hasSortedSetOfEnumChanged(), "sortedSetOfEnum");
        assertFalse(diff.hasSetOfStringChanged(), "setOfString");
        assertFalse(diff.hasHashSetOfStringChanged(), "hashSetOfString");
        assertTrue(diff.hasTreeSetOfStringChanged(), "treeSetOfString");
        assertFalse(diff.hasSortedSetOfStringChanged(), "sortedSetOfString");
        assertFalse(diff.hasListOfStringChanged(), "listOfString");
        assertFalse(diff.hasArrayListOfStringChanged(), "arrayListOfString");
        assertTrue(diff.hasLinkedListOfStringChanged(), "linkedListOfString");
        assertFalse(diff.hasVectorOfStringChanged(), "vectorOfString");
        assertTrue(diff.hasStackOfStringChanged(), "stackOfString");

        assertThat(diff.diffEnumSetOfEnum().addedElements())
            .isUnmodifiable()
            .hasSize(1)
            .containsExactly(AnEnum.TWO);

        assertThat(diff.diffEnumSetOfEnum().elementsInCommon())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffEnumSetOfEnum().removedElements())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffVectorOfString().addedElements())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffVectorOfString().elementsInCommon())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffVectorOfString().removedElements())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffStackOfString().addedElements())
            .isUnmodifiable()
            .hasSize(1)
            .containsExactly("A");

        assertThat(diff.diffLinkedListOfString().addedElements())
            .isUnmodifiable()
            .hasSize(2)
            .containsExactly("A", "A");

        // Let's merge two instances together!
        final NullableImmutableCollectionBag d = NullableImmutableCollectionBagUtils.merge(
            c,
            NullableImmutableCollectionBagUtils.builder()
                .addSetOfEnum(AnEnum.ONE)
                .addLinkedListOfString("B")
                .build()
        );

        assertNotNull(d);

        assertThat(d.setOfEnum())
            .isNotNull()
            .hasSize(1)
            .containsExactly(AnEnum.ONE);

        assertThat(d.enumSetOfEnum())
            .isNotNull()
            .hasSameElementsAs(c.enumSetOfEnum());

        assertThat(d.linkedListOfString())
            .isNotNull()
            .hasSize(3)
            .containsExactly("A", "A", "B");
    }

    @Test
    void nlAutoTest() {
        final NullableAutoCollectionBag a = NullableAutoCollectionBagUtils.builder()
            .build();
        // All of these should be null!
        assertNull(a.setOfEnum());
        assertNull(a.enumSetOfEnum());
        assertNull(a.hashSetOfEnum());
        assertNull(a.treeSetOfEnum());
        assertNull(a.sortedSetOfEnum());
        assertNull(a.setOfString());
        assertNull(a.hashSetOfString());
        assertNull(a.treeSetOfString());
        assertNull(a.sortedSetOfString());
        assertNull(a.listOfString());
        assertNull(a.arrayListOfString());
        assertNull(a.linkedListOfString());
        assertNull(a.vectorOfString());
        assertNull(a.stackOfString());
        // If I try and add something though it should be an EnumSet!
        final NullableAutoCollectionBag b = NullableAutoCollectionBagUtils.builder()
            .addSetOfEnum(AnEnum.ONE)
            .build();
        assertThat(b.setOfEnum())
            .isInstanceOf(EnumSet.class)
            .hasSize(1)
            .containsExactly(AnEnum.ONE);

        // OK, lets try and do a diff
        final NullableAutoCollectionBag c = NullableAutoCollectionBagUtils.builder()
            .addEnumSetOfEnum(AnEnum.TWO)
            .addHashSetOfEnum(Set.of(AnEnum.ONE, AnEnum.THREE))
            .addTreeSetOfString("This is a string!")
            .addLinkedListOfString(Stream.of("A").iterator())
            .addLinkedListOfString(Stream.of("A").spliterator())
            .addStackOfString(List.of("A"))
            .build();
        final var diff = NullableAutoCollectionBagUtils.diff(a, c);
        assertFalse(diff.hasSetOfEnumChanged(), "setOfEnum");
        assertTrue(diff.hasEnumSetOfEnumChanged(), "enumSetOfEnum");
        assertTrue(diff.hasHashSetOfEnumChanged(), "hashSetOfEnum");
        assertFalse(diff.hasTreeSetOfEnumChanged(), "treeSetOfEnum");
        assertFalse(diff.hasSortedSetOfEnumChanged(), "sortedSetOfEnum");
        assertFalse(diff.hasSetOfStringChanged(), "setOfString");
        assertFalse(diff.hasHashSetOfStringChanged(), "hashSetOfString");
        assertTrue(diff.hasTreeSetOfStringChanged(), "treeSetOfString");
        assertFalse(diff.hasSortedSetOfStringChanged(), "sortedSetOfString");
        assertFalse(diff.hasListOfStringChanged(), "listOfString");
        assertFalse(diff.hasArrayListOfStringChanged(), "arrayListOfString");
        assertTrue(diff.hasLinkedListOfStringChanged(), "linkedListOfString");
        assertFalse(diff.hasVectorOfStringChanged(), "vectorOfString");
        assertTrue(diff.hasStackOfStringChanged(), "stackOfString");

        assertThat(diff.diffEnumSetOfEnum().addedElements())
            .isUnmodifiable()
            .hasSize(1)
            .containsExactly(AnEnum.TWO);

        assertThat(diff.diffEnumSetOfEnum().elementsInCommon())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffEnumSetOfEnum().removedElements())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffVectorOfString().addedElements())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffVectorOfString().elementsInCommon())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffVectorOfString().removedElements())
            .isUnmodifiable()
            .isEmpty();

        assertThat(diff.diffStackOfString().addedElements())
            .isUnmodifiable()
            .hasSize(1)
            .containsExactly("A");

        assertThat(diff.diffLinkedListOfString().addedElements())
            .isUnmodifiable()
            .hasSize(2)
            .containsExactly("A", "A");

        // Let's merge two instances together!
        final NullableAutoCollectionBag d = NullableAutoCollectionBagUtils.merge(
            c,
            NullableAutoCollectionBagUtils.builder()
                .addSetOfEnum(AnEnum.ONE)
                .addLinkedListOfString("B")
                .build()
        );

        assertNotNull(d);

        assertThat(d.setOfEnum())
            .isNotNull()
            .hasSize(1)
            .containsExactly(AnEnum.ONE);

        assertThat(d.enumSetOfEnum())
            .isNotNull()
            .hasSameElementsAs(c.enumSetOfEnum());

        assertThat(d.linkedListOfString())
            .isNotNull()
            .hasSize(3)
            .containsExactly("A", "A", "B");
    }
}
