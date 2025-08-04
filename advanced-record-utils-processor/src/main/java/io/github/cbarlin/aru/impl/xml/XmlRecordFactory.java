package io.github.cbarlin.aru.impl.xml;

import java.util.Optional;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants;
import io.github.cbarlin.aru.impl.wiring.XmlPerRecordScope;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.github.cbarlin.aru.prism.prison.XmlOptionsPrism;
import io.github.cbarlin.aru.prism.prison.XmlRootElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlSchemaPrism;
import io.github.cbarlin.aru.prism.prison.XmlTypePrism;

@Factory
@XmlPerRecordScope
public final class XmlRecordFactory {

    private final AnalysedRecord analysedRecord;
    private final XmlOptionsPrism xmlOptionsPrism;

    public XmlRecordFactory(
        final AnalysedRecord analysedRecord,
        final AdvancedRecordUtilsPrism arup
    ) {
        this.analysedRecord = analysedRecord;
        this.xmlOptionsPrism = arup.xmlOptions();
    }

    @Bean
    XmlOptionsPrism xmlOptionsPrism() {
        return xmlOptionsPrism;
    }

    @Bean
    Optional<XmlTypePrism> xmlTypePrism() {
        return Optional.of(analysedRecord.typeElement())
                .filter(ign -> OptionalClassDetector.isAnnotationLoaded(Constants.Names.XML_TYPE))
                .flatMap(XmlTypePrism::getOptionalOn);
    }

    @Bean
    Optional<XmlRootElementPrism> xmlRootElementPrism() {
        return Optional.of(analysedRecord.typeElement())
                .filter(ign -> OptionalClassDetector.isAnnotationLoaded(Constants.Names.XML_ROOT_ELEMENT))
                .flatMap(XmlRootElementPrism::getOptionalOn);
    }

    @Bean
    Optional<XmlSchemaPrism> xmlSchemaPrism() {
        return findPackageElement(analysedRecord.typeElement())
                .filter(ign -> OptionalClassDetector.isAnnotationLoaded(Constants.Names.XML_SCHEMA))
                .flatMap(XmlSchemaPrism::getOptionalOn);
    }

    private static Optional<PackageElement> findPackageElement(final TypeElement typeElement) {
        final Element el = typeElement.getEnclosingElement();
        return switch (el) {
          case PackageElement packageElement -> Optional.of(packageElement);
          case TypeElement te -> findPackageElement(te);
          default -> Optional.empty();
        };
    }
}
