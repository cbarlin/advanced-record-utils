package io.github.cbarlin.aru.impl.xml;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Secondary;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.artifacts.PreBuilt;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedOptionalComponent;
import io.github.cbarlin.aru.impl.types.ComponentTargetingLibraryLoaded;
import io.github.cbarlin.aru.impl.types.dependencies.EclipseAnalysedCollectionComponent;
import io.github.cbarlin.aru.impl.wiring.XmlPerComponentScope;
import io.github.cbarlin.aru.impl.xml.inferencer.XmlAttributeMapper;
import io.github.cbarlin.aru.impl.xml.inferencer.XmlElementMapper;
import io.github.cbarlin.aru.impl.xml.inferencer.XmlElementWrapperMapper;
import io.github.cbarlin.aru.impl.xml.inferencer.XmlElementsMapper;
import io.github.cbarlin.aru.impl.xml.inferencer.XmlTransientMapper;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementWrapperPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementsPrism;
import io.github.cbarlin.aru.prism.prison.XmlTransientPrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Objects;
import java.util.Optional;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.COLLECTION;
import static io.github.cbarlin.aru.impl.Constants.Claims.XML_STATIC_CLASS;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_UTILS_CLASS;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__RICH_ITERABLE;

@Factory
@XmlPerComponentScope
public final class XmlComponentFactory {

    private final RecordComponentElement rce;

    public XmlComponentFactory(
        final RecordComponentElement rce
    ) {
        this.rce = rce;
    }

    @Bean
    Optional<XmlAttributePrism> xmlAttributePrism(final XmlAttributeMapper mapper) {
        return mapper.optionalInstanceOn(rce);
    }

    @Bean
    Optional<XmlElementPrism> xmlElementPrism(final XmlElementMapper mapper) {
        return mapper.optionalInstanceOn(rce);
    }

    @Bean
    Optional<XmlElementWrapperPrism> xmlElementWrapperPrism(final XmlElementWrapperMapper mapper) {
        return mapper.optionalInstanceOn(rce);
    }

    @Bean
    Optional<XmlTransientPrism> xmlTransientPrism(final XmlTransientMapper mapper) {
        return mapper.optionalInstanceOn(rce);
    }

    // Inferring this is expensive, so only do it if there's no other choice
    @Bean
    Optional<XmlElementsPrism> xmlElementsPrism(
        final XmlElementsMapper mapper,
        final Optional<XmlAttributePrism> attr,
        final Optional<XmlElementPrism> el,
        final Optional<XmlTransientPrism> trans
    ) {
        final var firstPass = mapper.optionalInstanceOn(rce);
        if (firstPass.isEmpty() && attr.isEmpty() && el.isEmpty() && trans.isEmpty()) {
            return mapper.inferAnnotationMirror(rce);
        }
        return firstPass;
    }

    @Bean
    Optional<LibraryLoadedXmlStaticHelper> findLibraryLoadedXmlStaticHelper(final Optional<ComponentTargetingLibraryLoaded> ctll) {
        return ctll.map(componentTargetingLibraryLoaded -> {
           final PreBuilt xmlChild = componentTargetingLibraryLoaded.target().utilsClassChild(XML_UTILS_CLASS, XML_STATIC_CLASS);
           if (Objects.nonNull(xmlChild)){
               return new LibraryLoadedXmlStaticHelper(xmlChild.className(), componentTargetingLibraryLoaded.target());
           }
           return null;
        });
    }

    @Bean
    @Secondary
    Optional<AnalysedCollectionComponent> unwrapOptional(final Optional<AnalysedOptionalComponent> opt) {
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        final AnalysedOptionalComponent component = opt.get();

        if (
            component.unNestedPrimaryTypeName() instanceof final ParameterizedTypeName ptn &&
            ptn.typeArguments.size() == 1 &&
            component.innerType() instanceof final DeclaredType declaredType &&
            declaredType.getTypeArguments().size() == 1
        ) {
            final TypeMirror innerType = declaredType.getTypeArguments().getFirst();
            final TypeName innerTypeName = ptn.typeArguments.getFirst();
            final ClassName erasedWrapperClassName = ptn.rawType;

            if (OptionalClassDetector.checkSameOrSubType(ptn.rawType, COLLECTION)) {
                return Optional.of(
                        new AnalysedCollectionComponent(component, innerType, innerTypeName, erasedWrapperClassName)
                );
            } else if (OptionalClassDetector.checkSameOrSubType(ptn.rawType, ECLIPSE_COLLECTIONS__RICH_ITERABLE)) {
                return Optional.of(
                        new EclipseAnalysedCollectionComponent(component, innerType, innerTypeName, erasedWrapperClassName)
                );
            }
        }

        return Optional.empty();
    }
}
