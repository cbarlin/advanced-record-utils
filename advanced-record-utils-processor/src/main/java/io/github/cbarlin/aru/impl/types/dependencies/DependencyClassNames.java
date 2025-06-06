package io.github.cbarlin.aru.impl.types.dependencies;

import io.micronaut.sourcegen.javapoet.ClassName;

public enum DependencyClassNames {
    ;
    public static final ClassName ECLIPSE_COLLECTIONS__RICH_ITERABLE = ClassName.get("org.eclipse.collections.api", "RichIterable");
    public static final ClassName ECLIPSE_COLLECTIONS__IMMUTABLE_COLLECTION = ClassName.get("org.eclipse.collections.api.collection", "ImmutableCollection");
    public static final ClassName ECLIPSE_COLLECTIONS__MUTABLE_COLLECTION = ClassName.get("org.eclipse.collections.api.collection", "MutableCollection");

    public static final ClassName ECLIPSE_COLLECTIONS__IMMUTABLE_LIST = ClassName.get("org.eclipse.collections.api.list", "ImmutableList");
    public static final ClassName ECLIPSE_COLLECTIONS__MUTABLE_LIST = ClassName.get("org.eclipse.collections.api.list", "MutableList");
    public static final ClassName ECLIPSE_COLLECTIONS__IMMUTABLE_SET = ClassName.get("org.eclipse.collections.api.set", "ImmutableSet");
    public static final ClassName ECLIPSE_COLLECTIONS__MUTABLE_SET = ClassName.get("org.eclipse.collections.api.set", "MutableSet");

}
