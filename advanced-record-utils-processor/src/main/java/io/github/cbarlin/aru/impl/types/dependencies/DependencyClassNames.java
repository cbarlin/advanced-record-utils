package io.github.cbarlin.aru.impl.types.dependencies;

import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.TypeName;

import java.util.HashMap;
import java.util.Map;

public enum DependencyClassNames {
    ;
    private static final String API_PATH = "org.eclipse.collections.api";
    public static final String PRIMITIVE_MAP_PACKAGE = "org.eclipse.collections.api.map.primitive";
    public static final String ITERATOR_PACKAGE = "org.eclipse.collections.api.iterator";
    public static final String PRIMITIVE_FACTORY_PACKAGE = "org.eclipse.collections.api.factory.primitive";
    public static final String PRIMITIVE_LIST_PACKAGE = "org.eclipse.collections.api.list.primitive";
    public static final String PRIMITIVE_SET_PACKAGE = "org.eclipse.collections.api.set.primitive";
    public static final String ECLIPSE_COLLECTIONS__PROPERTY = "class.exists.org.eclipse.collections.api/RichIterable";
    public static final ClassName ECLIPSE_COLLECTIONS__RICH_ITERABLE = ClassName.get(API_PATH, "RichIterable");

    public static final ClassName ECLIPSE_COLLECTIONS__PRIMITIVE_ITERABLE = ClassName.get(API_PATH, "PrimitiveIterable");
    public static final ClassName ECLIPSE_COLLECTIONS__BOOLEAN_ITERABLE = ClassName.get(API_PATH, "BooleanIterable");
    public static final ClassName ECLIPSE_COLLECTIONS__BYTE_ITERABLE = ClassName.get(API_PATH, "ByteIterable");
    public static final ClassName ECLIPSE_COLLECTIONS__CHAR_ITERABLE = ClassName.get(API_PATH, "CharIterable");
    public static final ClassName ECLIPSE_COLLECTIONS__DOUBLE_ITERABLE = ClassName.get(API_PATH, "DoubleIterable");
    public static final ClassName ECLIPSE_COLLECTIONS__FLOAT_ITERABLE = ClassName.get(API_PATH, "FloatIterable");
    public static final ClassName ECLIPSE_COLLECTIONS__INT_ITERABLE = ClassName.get(API_PATH, "IntIterable");
    public static final ClassName ECLIPSE_COLLECTIONS__LONG_ITERABLE = ClassName.get(API_PATH, "LongIterable");
    public static final ClassName ECLIPSE_COLLECTIONS__SHORT_ITERABLE = ClassName.get(API_PATH, "ShortIterable");
    
    public static final ClassName ECLIPSE_COLLECTIONS__BOOLEAN_ITERATOR = ClassName.get(ITERATOR_PACKAGE, "BooleanIterator");
    public static final ClassName ECLIPSE_COLLECTIONS__BYTE_ITERATOR = ClassName.get(ITERATOR_PACKAGE, "ByteIterator");
    public static final ClassName ECLIPSE_COLLECTIONS__CHAR_ITERATOR = ClassName.get(ITERATOR_PACKAGE, "CharIterator");
    public static final ClassName ECLIPSE_COLLECTIONS__DOUBLE_ITERATOR = ClassName.get(ITERATOR_PACKAGE, "DoubleIterator");
    public static final ClassName ECLIPSE_COLLECTIONS__FLOAT_ITERATOR = ClassName.get(ITERATOR_PACKAGE, "FloatIterator");
    public static final ClassName ECLIPSE_COLLECTIONS__INT_ITERATOR = ClassName.get(ITERATOR_PACKAGE, "IntIterator");
    public static final ClassName ECLIPSE_COLLECTIONS__LONG_ITERATOR = ClassName.get(ITERATOR_PACKAGE, "LongIterator");
    public static final ClassName ECLIPSE_COLLECTIONS__SHORT_ITERATOR = ClassName.get(ITERATOR_PACKAGE, "ShortIterator");

    public static final Map<TypeName, ClassName> ECLIPSE_COLLECTIONS_EXCHANGE__PRIMITIVE_ITERATOR;
    public static final Map<TypeName, ClassName> ECLIPSE_COLLECTIONS_EXCHANGE__PRIMITIVE_ITERABLE;

    static {
        final Map<TypeName, ClassName> iteratorMap = HashMap.newHashMap(16);
        iteratorMap.put(ECLIPSE_COLLECTIONS__SHORT_ITERABLE, ECLIPSE_COLLECTIONS__SHORT_ITERATOR);
        iteratorMap.put(ECLIPSE_COLLECTIONS__LONG_ITERABLE, ECLIPSE_COLLECTIONS__LONG_ITERATOR);
        iteratorMap.put(ECLIPSE_COLLECTIONS__INT_ITERABLE, ECLIPSE_COLLECTIONS__INT_ITERATOR);
        iteratorMap.put(ECLIPSE_COLLECTIONS__FLOAT_ITERABLE, ECLIPSE_COLLECTIONS__FLOAT_ITERATOR);
        iteratorMap.put(ECLIPSE_COLLECTIONS__DOUBLE_ITERABLE, ECLIPSE_COLLECTIONS__DOUBLE_ITERATOR);
        iteratorMap.put(ECLIPSE_COLLECTIONS__CHAR_ITERABLE, ECLIPSE_COLLECTIONS__CHAR_ITERATOR);
        iteratorMap.put(ECLIPSE_COLLECTIONS__BOOLEAN_ITERABLE, ECLIPSE_COLLECTIONS__BOOLEAN_ITERATOR);
        iteratorMap.put(ECLIPSE_COLLECTIONS__BYTE_ITERABLE, ECLIPSE_COLLECTIONS__BYTE_ITERATOR);
        iteratorMap.put(TypeName.SHORT, ECLIPSE_COLLECTIONS__SHORT_ITERATOR);
        iteratorMap.put(TypeName.LONG, ECLIPSE_COLLECTIONS__LONG_ITERATOR);
        iteratorMap.put(TypeName.INT, ECLIPSE_COLLECTIONS__INT_ITERATOR);
        iteratorMap.put(TypeName.FLOAT, ECLIPSE_COLLECTIONS__FLOAT_ITERATOR);
        iteratorMap.put(TypeName.DOUBLE, ECLIPSE_COLLECTIONS__DOUBLE_ITERATOR);
        iteratorMap.put(TypeName.CHAR, ECLIPSE_COLLECTIONS__CHAR_ITERATOR);
        iteratorMap.put(TypeName.BOOLEAN, ECLIPSE_COLLECTIONS__BOOLEAN_ITERATOR);
        iteratorMap.put(TypeName.BYTE, ECLIPSE_COLLECTIONS__BYTE_ITERATOR);
        ECLIPSE_COLLECTIONS_EXCHANGE__PRIMITIVE_ITERATOR = Map.copyOf(iteratorMap);
        final Map<TypeName, ClassName> iterableMap = HashMap.newHashMap(8);
        iterableMap.put(TypeName.SHORT, ECLIPSE_COLLECTIONS__SHORT_ITERABLE);
        iterableMap.put(TypeName.LONG, ECLIPSE_COLLECTIONS__LONG_ITERABLE);
        iterableMap.put(TypeName.INT, ECLIPSE_COLLECTIONS__INT_ITERABLE);
        iterableMap.put(TypeName.FLOAT, ECLIPSE_COLLECTIONS__FLOAT_ITERABLE);
        iterableMap.put(TypeName.DOUBLE, ECLIPSE_COLLECTIONS__DOUBLE_ITERABLE);
        iterableMap.put(TypeName.CHAR, ECLIPSE_COLLECTIONS__CHAR_ITERABLE);
        iterableMap.put(TypeName.BOOLEAN, ECLIPSE_COLLECTIONS__BOOLEAN_ITERABLE);
        iterableMap.put(TypeName.BYTE, ECLIPSE_COLLECTIONS__BYTE_ITERABLE);
        ECLIPSE_COLLECTIONS_EXCHANGE__PRIMITIVE_ITERABLE = Map.copyOf(iterableMap);
    }

    public static final ClassName ECLIPSE_COLLECTIONS__IMMUTABLE_LIST = ClassName.get("org.eclipse.collections.api.list", "ImmutableList");
    public static final ClassName ECLIPSE_COLLECTIONS__MUTABLE_LIST = ClassName.get("org.eclipse.collections.api.list", "MutableList");
    public static final ClassName ECLIPSE_COLLECTIONS__IMMUTABLE_SET = ClassName.get("org.eclipse.collections.api.set", "ImmutableSet");
    public static final ClassName ECLIPSE_COLLECTIONS__MUTABLE_SET = ClassName.get("org.eclipse.collections.api.set", "MutableSet");
    public static final ClassName ECLIPSE_COLLECTIONS__MULTIREADER_SET = ClassName.get("org.eclipse.collections.api.set", "MultiReaderSet");

    public static final ClassName ECLIPSE_COLLECTIONS__SETS_FACTORY = ClassName.get("org.eclipse.collections.api.factory", "Sets");
    public static final ClassName ECLIPSE_COLLECTIONS__LISTS_FACTORY = ClassName.get("org.eclipse.collections.api.factory", "Lists");

    public static final ClassName ECLIPSE_COLLECTIONS_MAP_ITERABLE = ClassName.get("org.eclipse.collections.api.map", "MapIterable");
}
