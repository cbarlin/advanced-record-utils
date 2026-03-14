package io.github.cbarlin.aru.impl.types.dependencies;

import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.impl.Constants;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;
import org.apache.commons.lang3.tuple.Pair;

import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum DependencyClassNames {
    ;
    // region HPPC
    public static final String HPPC__PACKAGE = "com.carrotsearch.hppc";
    public static final String HPPC__PROPERTY = "class.exists.com.carrotsearch.hppc/Accountable";
    public static final ClassName HPPC__ACCOUNTABLE = ClassName.get(HPPC__PACKAGE, "Accountable");

    public static final ClassName HPPC__BYTE_CURSOR = ClassName.get("com.carrotsearch.hppc.cursors", "ByteCursor");
    public static final ClassName HPPC__CHAR_CURSOR = ClassName.get("com.carrotsearch.hppc.cursors", "CharCursor");
    public static final ClassName HPPC__DOUBLE_CURSOR = ClassName.get("com.carrotsearch.hppc.cursors", "DoubleCursor");
    public static final ClassName HPPC__FLOAT_CURSOR = ClassName.get("com.carrotsearch.hppc.cursors", "FloatCursor");
    public static final ClassName HPPC__INT_CURSOR = ClassName.get("com.carrotsearch.hppc.cursors", "IntCursor");
    public static final ClassName HPPC__LONG_CURSOR = ClassName.get("com.carrotsearch.hppc.cursors", "LongCursor");
    public static final ClassName HPPC__SHORT_CURSOR = ClassName.get("com.carrotsearch.hppc.cursors", "ShortCursor");

    public static final Map<String, ClassName> HPPC__PRIMITIVE_NAME_TO_CURSOR = Map.ofEntries(
            Map.entry("Byte", HPPC__BYTE_CURSOR),
            Map.entry("Char", HPPC__CHAR_CURSOR),
            Map.entry("Double", HPPC__DOUBLE_CURSOR),
            Map.entry("Float", HPPC__FLOAT_CURSOR),
            Map.entry("Int", HPPC__INT_CURSOR),
            Map.entry("Long", HPPC__LONG_CURSOR),
            Map.entry("Short", HPPC__SHORT_CURSOR)
    );

    public static final Map<TypeName, TypeName> HPPC__ITERATOR = Map.ofEntries(
            Map.entry(TypeName.BYTE, ParameterizedTypeName.get(Constants.Names.ITERATOR, HPPC__BYTE_CURSOR)),
            Map.entry(TypeName.CHAR, ParameterizedTypeName.get(Constants.Names.ITERATOR, HPPC__CHAR_CURSOR)),
            Map.entry(TypeName.DOUBLE, ParameterizedTypeName.get(Constants.Names.ITERATOR, HPPC__DOUBLE_CURSOR)),
            Map.entry(TypeName.FLOAT, ParameterizedTypeName.get(Constants.Names.ITERATOR, HPPC__FLOAT_CURSOR)),
            Map.entry(TypeName.INT, ParameterizedTypeName.get(Constants.Names.ITERATOR, HPPC__INT_CURSOR)),
            Map.entry(TypeName.LONG, ParameterizedTypeName.get(Constants.Names.ITERATOR, HPPC__LONG_CURSOR)),
            Map.entry(TypeName.SHORT, ParameterizedTypeName.get(Constants.Names.ITERATOR, HPPC__SHORT_CURSOR))
    );

    public static final Map<TypeName, Pair<TypeName, TypeMirror>> HPPC__NAME_TO_PRIMITIVE;

    static {
        final Map<TypeName, Pair<TypeName, TypeMirror>> names = HashMap.newHashMap(Constants.Names.NON_BOOL_PRIMITIVES.size() * 4 - 2);
        for (final TypeName primitive : Constants.Names.NON_BOOL_PRIMITIVES) {
            final String firstCap = capitalise(primitive.toString());
            final Pair<TypeName, TypeMirror> pair = Pair.of(primitive, APContext.types().getPrimitiveType(Constants.Names.PRIMITIVE_TYPE_NAME_TO_TYPE_KIND.get(primitive)));
            names.put(ClassName.get(HPPC__PACKAGE, firstCap + "ArrayList"), pair);
            names.put(ClassName.get(HPPC__PACKAGE, firstCap + "Stack"), pair);
            if (!"Byte".equals(firstCap)) {
                names.put(ClassName.get(HPPC__PACKAGE, firstCap + "HashSet"), pair);
                names.put(ClassName.get(HPPC__PACKAGE, firstCap + "Set"), pair);
            }
        }
        HPPC__NAME_TO_PRIMITIVE = Map.copyOf(names);
    }

    // endregion
    // region Eclipse
    private static final String ECLIPSE_API_PATH = "org.eclipse.collections.api";
    public static final String ECLIPSE_COLLECTIONS__PRIMITIVE_MAP_PACKAGE = "org.eclipse.collections.api.map.primitive";
    public static final String ECLIPSE_COLLECTIONS__ITERATOR_PACKAGE = "org.eclipse.collections.api.iterator";
    public static final String ECLIPSE_COLLECTIONS__PRIMITIVE_FACTORY_PACKAGE = "org.eclipse.collections.api.factory.primitive";
    public static final String ECLIPSE_COLLECTIONS__PRIMITIVE_LIST_PACKAGE = "org.eclipse.collections.api.list.primitive";
    public static final String ECLIPSE_COLLECTIONS__PRIMITIVE_SET_PACKAGE = "org.eclipse.collections.api.set.primitive";
    public static final String ECLIPSE_COLLECTIONS__PROPERTY = "class.exists.org.eclipse.collections.api/RichIterable";
    public static final ClassName ECLIPSE_COLLECTIONS__RICH_ITERABLE = ClassName.get(ECLIPSE_API_PATH, "RichIterable");

    public static final ClassName ECLIPSE_COLLECTIONS__PRIMITIVE_ITERABLE = ClassName.get(ECLIPSE_API_PATH, "PrimitiveIterable");
    public static final ClassName ECLIPSE_COLLECTIONS__BOOLEAN_ITERABLE = ClassName.get(ECLIPSE_API_PATH, "BooleanIterable");
    public static final ClassName ECLIPSE_COLLECTIONS__BYTE_ITERABLE = ClassName.get(ECLIPSE_API_PATH, "ByteIterable");
    public static final ClassName ECLIPSE_COLLECTIONS__CHAR_ITERABLE = ClassName.get(ECLIPSE_API_PATH, "CharIterable");
    public static final ClassName ECLIPSE_COLLECTIONS__DOUBLE_ITERABLE = ClassName.get(ECLIPSE_API_PATH, "DoubleIterable");
    public static final ClassName ECLIPSE_COLLECTIONS__FLOAT_ITERABLE = ClassName.get(ECLIPSE_API_PATH, "FloatIterable");
    public static final ClassName ECLIPSE_COLLECTIONS__INT_ITERABLE = ClassName.get(ECLIPSE_API_PATH, "IntIterable");
    public static final ClassName ECLIPSE_COLLECTIONS__LONG_ITERABLE = ClassName.get(ECLIPSE_API_PATH, "LongIterable");
    public static final ClassName ECLIPSE_COLLECTIONS__SHORT_ITERABLE = ClassName.get(ECLIPSE_API_PATH, "ShortIterable");
    
    public static final ClassName ECLIPSE_COLLECTIONS__BOOLEAN_ITERATOR = ClassName.get(ECLIPSE_COLLECTIONS__ITERATOR_PACKAGE, "BooleanIterator");
    public static final ClassName ECLIPSE_COLLECTIONS__BYTE_ITERATOR = ClassName.get(ECLIPSE_COLLECTIONS__ITERATOR_PACKAGE, "ByteIterator");
    public static final ClassName ECLIPSE_COLLECTIONS__CHAR_ITERATOR = ClassName.get(ECLIPSE_COLLECTIONS__ITERATOR_PACKAGE, "CharIterator");
    public static final ClassName ECLIPSE_COLLECTIONS__DOUBLE_ITERATOR = ClassName.get(ECLIPSE_COLLECTIONS__ITERATOR_PACKAGE, "DoubleIterator");
    public static final ClassName ECLIPSE_COLLECTIONS__FLOAT_ITERATOR = ClassName.get(ECLIPSE_COLLECTIONS__ITERATOR_PACKAGE, "FloatIterator");
    public static final ClassName ECLIPSE_COLLECTIONS__INT_ITERATOR = ClassName.get(ECLIPSE_COLLECTIONS__ITERATOR_PACKAGE, "IntIterator");
    public static final ClassName ECLIPSE_COLLECTIONS__LONG_ITERATOR = ClassName.get(ECLIPSE_COLLECTIONS__ITERATOR_PACKAGE, "LongIterator");
    public static final ClassName ECLIPSE_COLLECTIONS__SHORT_ITERATOR = ClassName.get(ECLIPSE_COLLECTIONS__ITERATOR_PACKAGE, "ShortIterator");

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

    public static final ClassName ECLIPSE_COLLECTIONS__SETS_FACTORY = ClassName.get("org.eclipse.collections.api.factory", "Sets");
    public static final ClassName ECLIPSE_COLLECTIONS__LISTS_FACTORY = ClassName.get("org.eclipse.collections.api.factory", "Lists");

    public static final ClassName ECLIPSE_COLLECTIONS_MAP_ITERABLE = ClassName.get("org.eclipse.collections.api.map", "MapIterable");
    // endregion

    private static String capitalise(final String variableName) {
        return (variableName.length() < 2) ? variableName.toUpperCase(Locale.ROOT)
                : (Character.toUpperCase(variableName.charAt(0)) + variableName.substring(1));
    }
}
