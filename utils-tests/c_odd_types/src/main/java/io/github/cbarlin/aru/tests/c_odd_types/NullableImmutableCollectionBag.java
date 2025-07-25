package io.github.cbarlin.aru.tests.c_odd_types;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuilderOptions;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.NameGeneration;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.XmlOptions;

@AdvancedRecordUtils(
    builderOptions = @BuilderOptions(
        // This should be the default
        // builtCollectionType = BuiltCollectionType.JAVA_IMMUTABLE,
        buildNullCollectionToEmpty = false
    ),
    diffable = true,
    xmlable = true,
    xmlOptions = @XmlOptions(
        inferXmlElementName = NameGeneration.MATCH
    ),
    merger = true,
    wither = true
)
public record NullableImmutableCollectionBag(
    Set<AnEnum> setOfEnum,
    EnumSet<AnEnum> enumSetOfEnum,
    HashSet<AnEnum> hashSetOfEnum,
    TreeSet<AnEnum> treeSetOfEnum,
    SortedSet<AnEnum> sortedSetOfEnum,

    Set<String> setOfString,
    HashSet<String> hashSetOfString,
    TreeSet<String> treeSetOfString,
    SortedSet<String> sortedSetOfString,

    List<String> listOfString,
    ArrayList<String> arrayListOfString,
    LinkedList<String> linkedListOfString,
    Vector<String> vectorOfString,
    Stack<String> stackOfString
) {

}
