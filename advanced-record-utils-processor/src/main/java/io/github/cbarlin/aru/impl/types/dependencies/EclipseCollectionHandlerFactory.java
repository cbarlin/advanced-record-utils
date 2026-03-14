package io.github.cbarlin.aru.impl.types.dependencies;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.impl.types.collection.eclipse.EclipseCollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.eclipse.list.EclipseImmutableList;
import io.github.cbarlin.aru.impl.types.collection.eclipse.list.EclipseMutableList;
import io.github.cbarlin.aru.impl.types.collection.eclipse.list.EclipsePrimitiveList;
import io.github.cbarlin.aru.impl.types.collection.eclipse.set.EclipseImmutableSet;
import io.github.cbarlin.aru.impl.types.collection.eclipse.set.EclipseMutableSet;
import io.github.cbarlin.aru.impl.types.collection.eclipse.set.EclipsePrimitiveSet;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.ClassName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__PRIMITIVE_FACTORY_PACKAGE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__PRIMITIVE_LIST_PACKAGE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__PRIMITIVE_SET_PACKAGE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__PROPERTY;

@Factory
@GlobalScope
@RequiresProperty(value = ECLIPSE_COLLECTIONS__PROPERTY, equalTo = "true")
public final class EclipseCollectionHandlerFactory {

    @Bean
    public List<EclipseCollectionHandler> eclipseHandlers() {
        final List<EclipseCollectionHandler> standard = standardCollectionHandlers();
        final List<EclipseCollectionHandler> primitiveList = primitiveListCollectionHandlers();
        final List<EclipseCollectionHandler> primitiveSet = primitiveSetCollectionHandlers();
        final List<EclipseCollectionHandler> result = new ArrayList<>(
                standard.size() + primitiveList.size() + primitiveSet.size()
        );

        result.addAll(standard);
        result.addAll(primitiveList);
        result.addAll(primitiveSet);

        return Collections.unmodifiableList(result);
    }

    private List<EclipseCollectionHandler> standardCollectionHandlers() {
        return List.of(
                new EclipseImmutableList(),
                new EclipseMutableList(),
                new EclipseImmutableSet(),
                new EclipseMutableSet()
        );
    }

    private List<EclipseCollectionHandler> primitiveListCollectionHandlers() {
        return List.of(
                primitiveList("ImmutableShortList", "Short"),
                primitiveList("MutableShortList", "Short"),
                primitiveList("ImmutableLongList", "Long"),
                primitiveList("MutableLongList", "Long"),
                primitiveList("ImmutableFloatList", "Float"),
                primitiveList("MutableFloatList", "Float"),
                primitiveList("ImmutableIntList", "Int"),
                primitiveList("MutableIntList", "Int"),
                primitiveList("ImmutableCharList", "Char"),
                primitiveList("MutableCharList", "Char"),
                primitiveList("ImmutableDoubleList", "Double"),
                primitiveList("MutableDoubleList", "Double"),
                primitiveList("ImmutableByteList", "Byte"),
                primitiveList("MutableByteList", "Byte"),
                primitiveList("ImmutableBooleanList", "Boolean"),
                primitiveList("MutableBooleanList", "Boolean"),
                primitiveList("ShortList", "Short"),
                primitiveList("ByteList", "Byte"),
                primitiveList("BooleanList", "Boolean"),
                primitiveList("DoubleList", "Double"),
                primitiveList("CharList", "Char"),
                primitiveList("FloatList", "Float"),
                primitiveList("IntList", "Int"),
                primitiveList("LongList", "Long")
        );
    }


    private List<EclipseCollectionHandler> primitiveSetCollectionHandlers() {
        return List.of(
                primitiveSet("ImmutableShortSet", "Short"),
                primitiveSet("MutableShortSet", "Short"),
                primitiveSet("ImmutableLongSet", "Long"),
                primitiveSet("MutableLongSet", "Long"),
                primitiveSet("ImmutableFloatSet", "Float"),
                primitiveSet("MutableFloatSet", "Float"),
                primitiveSet("ImmutableIntSet", "Int"),
                primitiveSet("MutableIntSet", "Int"),
                primitiveSet("ImmutableCharSet", "Char"),
                primitiveSet("MutableCharSet", "Char"),
                primitiveSet("ImmutableDoubleSet", "Double"),
                primitiveSet("MutableDoubleSet", "Double"),
                primitiveSet("ImmutableByteSet", "Byte"),
                primitiveSet("MutableByteSet", "Byte"),
                primitiveSet("ImmutableBooleanSet", "Boolean"),
                primitiveSet("MutableBooleanSet", "Boolean"),
                primitiveSet("ShortSet", "Short"),
                primitiveSet("BooleanSet", "Boolean"),
                primitiveSet("ByteSet", "Byte"),
                primitiveSet("CharSet", "Char"),
                primitiveSet("DoubleSet", "Double"),
                primitiveSet("FloatSet", "Float"),
                primitiveSet("IntSet", "Int"),
                primitiveSet("LongSet", "Long")
        );
    }

    private static EclipsePrimitiveSet primitiveSet(final String simpleName, final String typeName) {
        return new EclipsePrimitiveSet(
                ClassName.get(ECLIPSE_COLLECTIONS__PRIMITIVE_SET_PACKAGE, simpleName),
                ClassName.get(ECLIPSE_COLLECTIONS__PRIMITIVE_SET_PACKAGE, "Mutable" + typeName + "Set"),
                ClassName.get(ECLIPSE_COLLECTIONS__PRIMITIVE_SET_PACKAGE, "Immutable" + typeName + "Set"),
                ClassName.get(ECLIPSE_COLLECTIONS__PRIMITIVE_FACTORY_PACKAGE, typeName + "Sets")
        );
    }

    private static EclipsePrimitiveList primitiveList(final String simpleName, final String typeName) {
        return new EclipsePrimitiveList(
                ClassName.get(ECLIPSE_COLLECTIONS__PRIMITIVE_LIST_PACKAGE, simpleName),
                ClassName.get(ECLIPSE_COLLECTIONS__PRIMITIVE_LIST_PACKAGE, "Mutable" + typeName + "List"),
                ClassName.get(ECLIPSE_COLLECTIONS__PRIMITIVE_LIST_PACKAGE, "Immutable" + typeName + "List"),
                ClassName.get(ECLIPSE_COLLECTIONS__PRIMITIVE_FACTORY_PACKAGE, typeName + "Lists"),
                ClassName.get(ECLIPSE_COLLECTIONS__PRIMITIVE_SET_PACKAGE, "Mutable" + typeName + "Set")
        );
    }
}
