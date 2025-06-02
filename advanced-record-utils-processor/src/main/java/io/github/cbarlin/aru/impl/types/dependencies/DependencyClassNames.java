package io.github.cbarlin.aru.impl.types.dependencies;

import io.micronaut.sourcegen.javapoet.ClassName;

public enum DependencyClassNames {
    ;
    private static final String ECLIPSE_COLLECTIONS_API = "org.eclipse.collections.api";
    public static final ClassName ECLIPSE_COLLECTIONS__RICH_ITERABLE = ClassName.get(ECLIPSE_COLLECTIONS_API, "RichIterable");
    public static final ClassName ECLIPSE_COLLECTIONS__IMMUTABLE_COLLECTION = ClassName.get(ECLIPSE_COLLECTIONS_API + ".collection", "ImmutableCollection");
    public static final ClassName ECLIPSE_COLLECTIONS__MUTABLE_COLLECTION = ClassName.get(ECLIPSE_COLLECTIONS_API + ".collection", "MutableCollection");

    public static final ClassName ECLIPSE_COLLECTIONS__IMMUTABLE_LIST = ClassName.get(ECLIPSE_COLLECTIONS_API + ".list", "ImmutableList");
    public static final ClassName ECLIPSE_COLLECTIONS__MUTABLE_LIST = ClassName.get(ECLIPSE_COLLECTIONS_API + ".list", "MutableList");
    public static final ClassName ECLIPSE_COLLECTIONS__IMMUTABLE_SET = ClassName.get(ECLIPSE_COLLECTIONS_API + ".set", "ImmutableSet");
    public static final ClassName ECLIPSE_COLLECTIONS__MUTABLE_SET = ClassName.get(ECLIPSE_COLLECTIONS_API + ".set", "MutableSet");

    private static final String FASTUTILS_PACKAGE = "it.unimi.dsi.fastutil";
    public static final ClassName FASTUTILS__BOOLEAN_COLLECTION = ClassName.get(FASTUTILS_PACKAGE + ".booleans", "BooleanCollection");
    public static final ClassName FASTUTILS__BYTE_COLLECTION = ClassName.get(FASTUTILS_PACKAGE + ".bytes", "ByteCollection");
    public static final ClassName FASTUTILS__CHAR_COLLECTION = ClassName.get(FASTUTILS_PACKAGE + ".chars", "CharCollection");
    public static final ClassName FASTUTILS__DOUBLE_COLLECTION = ClassName.get(FASTUTILS_PACKAGE + ".doubles", "DoubleCollection");
    public static final ClassName FASTUTILS__FLOAT_COLLECTION = ClassName.get(FASTUTILS_PACKAGE + ".floats", "FloatCollection");
    public static final ClassName FASTUTILS__INT_COLLECTION = ClassName.get(FASTUTILS_PACKAGE + ".ints", "IntCollection");
    public static final ClassName FASTUTILS__LONG_COLLECTION = ClassName.get(FASTUTILS_PACKAGE + ".longs", "LongCollection");
    public static final ClassName FASTUTILS__SHORT_COLLECTION = ClassName.get(FASTUTILS_PACKAGE + ".shorts", "ShortCollection");
}
