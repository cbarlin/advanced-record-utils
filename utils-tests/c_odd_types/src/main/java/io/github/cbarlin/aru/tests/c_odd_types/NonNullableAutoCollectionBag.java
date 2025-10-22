package io.github.cbarlin.aru.tests.c_odd_types;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuilderOptions;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuiltCollectionType;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.NameGeneration;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.XmlOptions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;

@AdvancedRecordUtils(
    builderOptions = @BuilderOptions(
        builtCollectionType = BuiltCollectionType.AUTO
        // This should be the default:
        // buildNullCollectionToEmpty = true
    ),
    diffable = true,
    xmlable = true,
    xmlOptions = @XmlOptions(
        inferXmlElementName = NameGeneration.MATCH
    ),
    merger = true,
    mergerOptions = @AdvancedRecordUtils.MergerOptions(
        staticMethodsAddedToUtils = true
    ),
    diffOptions = @AdvancedRecordUtils.DiffOptions(
        staticMethodsAddedToUtils = true
    ),
    wither = true
)
public record NonNullableAutoCollectionBag(
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
    Stack<String> stackOfString,

    Queue<AnEnum> queueOfEnum,
    Deque<AnEnum> dequeOfEnum,
    ArrayDeque<String> arrayDeque,
    ConcurrentLinkedQueue<String> concurrentLinkedQueue,
    ConcurrentLinkedDeque<String> concurrentLinkedDeque,
    LinkedBlockingDeque<String> linkedBlockingDeque,
    LinkedBlockingQueue<String> linkedBlockingQueue,
    LinkedTransferQueue<String> linkedTransferQueue,
    PriorityBlockingQueue<String> priorityBlockingQueue,
    SynchronousQueue<String> synchronousQueue,
    PriorityQueue<String> priorityQueue,
    LinkedHashSet<String> linkedHashSet,
    ConcurrentSkipListSet<String> concurrentSkipListSet,
    CopyOnWriteArraySet<String> copyOnWriteArraySet,
    CopyOnWriteArrayList<String> copyOnWriteArrayList
) {

}
