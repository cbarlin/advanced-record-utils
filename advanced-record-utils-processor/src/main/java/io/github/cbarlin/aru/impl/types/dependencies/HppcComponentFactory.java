package io.github.cbarlin.aru.impl.types.dependencies;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.BasicAnalysedComponent;
import io.github.cbarlin.aru.impl.types.AnalysedPrimitiveCollectionComponent;
import io.github.cbarlin.aru.impl.wiring.BasePerComponentScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.TypeName;
import org.apache.commons.lang3.tuple.Pair;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.HPPC__ACCOUNTABLE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.HPPC__NAME_TO_PRIMITIVE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.HPPC__PROPERTY;

@Factory
@BasePerComponentScope
@RequiresProperty(value = HPPC__PROPERTY, equalTo = "true")
public final class HppcComponentFactory {


    @Bean
    @BeanTypes({HppcPrimitiveCollectionComponent.class, AnalysedPrimitiveCollectionComponent.class, AnalysedCollectionComponent.class})
    Optional<HppcPrimitiveCollectionComponent> analyse(
            final RecordComponentElement element,
            final BasicAnalysedComponent basicAnalysedComponent
    ) {
        if (
                basicAnalysedComponent.typeName() instanceof ClassName &&
                OptionalClassDetector.checkSameOrSubType(element, HPPC__ACCOUNTABLE)
        ) {
            final TypeName searchType = basicAnalysedComponent.typeNameWithoutAnnotations();
            if (HPPC__NAME_TO_PRIMITIVE.containsKey(searchType) && searchType instanceof final ClassName scn) {
                final Pair<TypeName, TypeMirror> pair = HPPC__NAME_TO_PRIMITIVE.get(searchType);
                return Optional.of(new HppcPrimitiveCollectionComponent(
                        basicAnalysedComponent,
                        pair.getRight(),
                        pair.getLeft(),
                        scn
                ));
            }
        }
        return Optional.empty();
    }

}
