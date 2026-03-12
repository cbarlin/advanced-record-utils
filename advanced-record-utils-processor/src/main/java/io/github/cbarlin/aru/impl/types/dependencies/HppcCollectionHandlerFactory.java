package io.github.cbarlin.aru.impl.types.dependencies;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.hppc.HppcPrimitiveList;
import io.github.cbarlin.aru.impl.types.collection.hppc.HppcPrimitiveSet;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.ClassName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.HPPC__PACKAGE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.HPPC__PRIMITIVE_NAME_TO_CURSOR;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.HPPC__PROPERTY;

@Factory
@GlobalScope
@RequiresProperty(value = HPPC__PROPERTY, equalTo = "true")
public class HppcCollectionHandlerFactory {

    // Do not handle `Byte` since there's no "set" version of that to do diffs with...
    private static final List<String> NAMES = List.of("Char", "Double", "Float", "Int", "Long", "Short");

    @Bean
    List<CollectionHandler> hppcCollectionHandlers() {
        final List<CollectionHandler> handlers = new ArrayList<>();
        for (final String name : NAMES) {
            final ClassName setName = ClassName.get(HPPC__PACKAGE, name + "HashSet");
            // Not that this can ever be null, since the map's keys are a superset of the names we are iterating through
            final ClassName cursorName = Objects.requireNonNull(HPPC__PRIMITIVE_NAME_TO_CURSOR.get(name));
            handlers.add(new HppcPrimitiveSet(List.of(ClassName.get(HPPC__PACKAGE, name + "Set"), setName), setName, cursorName));
            final ClassName mapToLongName = ClassName.get(HPPC__PACKAGE, name + "LongHashMap");
            final ClassName concreteList = ClassName.get(HPPC__PACKAGE, name + "ArrayList");
            handlers.add(new HppcPrimitiveList(
                    List.of(
                        ClassName.get(HPPC__PACKAGE, name + "ArrayList"),
                        ClassName.get(HPPC__PACKAGE, name + "IndexedContainer")
                    ),
                    concreteList,
                    setName,
                    mapToLongName,
                    cursorName
            ));
            handlers.add(new HppcPrimitiveList(
                    List.of(
                            ClassName.get(HPPC__PACKAGE, name + "Stack")
                    ),
                    ClassName.get(HPPC__PACKAGE, name + "Stack"),
                    setName,
                    mapToLongName,
                    cursorName
            ));
        }
        return handlers;
    }

}
