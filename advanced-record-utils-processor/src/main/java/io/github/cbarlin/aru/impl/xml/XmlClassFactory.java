package io.github.cbarlin.aru.impl.xml;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.UtilsClass;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.XmlPerRecordScope;
import io.github.cbarlin.aru.impl.xml.inferencer.XmlAttributeMapper;
import io.github.cbarlin.aru.impl.xml.inferencer.XmlElementMapper;
import io.github.cbarlin.aru.impl.xml.inferencer.XmlElementWrapperMapper;
import io.github.cbarlin.aru.impl.xml.inferencer.XmlElementsMapper;
import io.github.cbarlin.aru.impl.xml.inferencer.XmlTransientMapper;
import io.github.cbarlin.aru.prism.prison.XmlAccessorOrderPrism;
import io.github.cbarlin.aru.prism.prison.XmlOptionsPrism;
import io.github.cbarlin.aru.prism.prison.XmlRootElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlSchemaPrism;
import io.github.cbarlin.aru.prism.prison.XmlTypePrism;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.TypeElement;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_DEFAULT_STRING;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_UTILS_CLASS;

@Factory
@XmlPerRecordScope
public final class XmlClassFactory {

    public static final Set<String> VALID_XML_ACCESS_ORDERS = Set.of("UNDEFINED", "ALPHABETICAL");
    public static final String STATIC_WRITE_XML_NAME = "writeToXml";

    @Bean
    XmlRecordHolder xmlHolder(
        final AnalysedRecord analysedRecord, 
        final UtilsClass utilsClass,
        final Optional<XmlTypePrism> xmlTypePrism,
        final Optional<XmlRootElementPrism> xmlRootElementPrism,
        final Optional<XmlSchemaPrism> xmlSchemaPrism,
        final XmlElementWrapperMapper xmlElementWrapperMapper,
        final XmlElementsMapper xmlElementsMapper,
        final XmlElementMapper xmlElementMapper,
        final XmlAttributeMapper xmlAttributeMapper,
        final XmlTransientMapper xmlTransientMapper
    ) {
        final XmlOptionsPrism xmlOptionsPrism = analysedRecord.settings().prism().xmlOptions();
        final String xmlInterfaceName = xmlOptionsPrism.xmlName();
        final ToBeBuilt xmlInterface = utilsClass.childInterfaceArtifact(xmlInterfaceName, Claims.XML_IFACE);
        xmlInterface.builder().addAnnotation(CommonsConstants.Names.NULL_MARKED);
        final ToBeBuilt xmlStaticClass = utilsClass.childClassArtifact(XML_UTILS_CLASS, Claims.XML_STATIC_CLASS);
        xmlStaticClass.builder().addAnnotation(CommonsConstants.Names.NULL_MARKED);

        return new XmlRecordHolder(
            xmlInterface,
            xmlStaticClass,
            xmlOptionsPrism,
            analysedRecord,
            namespace(xmlTypePrism, xmlRootElementPrism, xmlSchemaPrism),
            elementName(analysedRecord, xmlTypePrism, xmlRootElementPrism),
            isAlphabetical(analysedRecord.typeElement()),
            xmlInterface.createMethod(xmlOptionsPrism.continueAddingToXmlMethodName(), Claims.XML_IFACE_TO_XML),
            xmlStaticClass.createMethod(STATIC_WRITE_XML_NAME, Claims.XML_STATIC_CLASS_TO_XML),
            xmlElementWrapperMapper,
            xmlElementsMapper,
            xmlElementMapper,
            xmlAttributeMapper,
            xmlTransientMapper
        );
    }

    private static boolean isAlphabetical(final TypeElement recordElement) {
        return "ALPHABETICAL".equals(XmlAccessorOrderPrism.getOptionalOn(recordElement)
            .map(XmlAccessorOrderPrism::value)
            .filter(StringUtils::isNotBlank)
            .filter(VALID_XML_ACCESS_ORDERS::contains)
            .or(
                () -> XmlAccessorOrderPrism.getOptionalOn(recordElement.getEnclosingElement())
                        .map(XmlAccessorOrderPrism::value)
                        .filter(StringUtils::isNotBlank)
                        .filter(VALID_XML_ACCESS_ORDERS::contains)
            )
            .orElse("UNDEFINED"));
    }

    private static Optional<String> namespace(final Optional<XmlTypePrism> xmlTypePrism,
            final Optional<XmlRootElementPrism> xmlRootElementPrism, final Optional<XmlSchemaPrism> xmlSchemaPrism) {
        return xmlTypePrism.map(XmlTypePrism::namespace)
            .filter(StringUtils::isNotBlank)
            .filter(Predicate.not(XML_DEFAULT_STRING::equals))
            .or(
                () -> xmlRootElementPrism.map(XmlRootElementPrism::namespace)
                        .filter(StringUtils::isNotBlank)
                        .filter(Predicate.not(XML_DEFAULT_STRING::equals))
            )
            .or(
                () -> xmlSchemaPrism.map(XmlSchemaPrism::namespace)
                        .filter(StringUtils::isNotBlank)
                        .filter(Predicate.not(XML_DEFAULT_STRING::equals))
            );
    }

    private static String elementName(final AnalysedRecord analysedRecord, final Optional<XmlTypePrism> xmlTypePrism,
            final Optional<XmlRootElementPrism> xmlRootElementPrism) {
        return xmlTypePrism.map(XmlTypePrism::name)
            .filter(StringUtils::isNotBlank)
            .filter(Predicate.not(XML_DEFAULT_STRING::equals))
            .or(
                () -> xmlRootElementPrism.map(XmlRootElementPrism::name)
                    .filter(StringUtils::isNotBlank)
                    .filter(Predicate.not(XML_DEFAULT_STRING::equals))
            )
            .orElseGet(analysedRecord::typeSimpleName);
    }
}
