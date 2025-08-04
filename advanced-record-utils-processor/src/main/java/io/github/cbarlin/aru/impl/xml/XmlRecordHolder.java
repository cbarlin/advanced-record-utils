package io.github.cbarlin.aru.impl.xml;

import java.util.Optional;

import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.xml.inferencer.XmlAttributeMapper;
import io.github.cbarlin.aru.impl.xml.inferencer.XmlElementMapper;
import io.github.cbarlin.aru.impl.xml.inferencer.XmlElementWrapperMapper;
import io.github.cbarlin.aru.impl.xml.inferencer.XmlElementsMapper;
import io.github.cbarlin.aru.impl.xml.inferencer.XmlTransientMapper;
import io.github.cbarlin.aru.prism.prison.XmlOptionsPrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;

/**
 * A record that's used to hold a bunch of dependencies
 */
public record XmlRecordHolder(
    ToBeBuilt xmlInterface,
    ToBeBuilt xmlStaticClass,
    XmlOptionsPrism prism,
    AnalysedRecord analysedRecord,
    Optional<String> namespace,
    String elementName,
    boolean isAlphabeticalAccessOrder,
    MethodSpec.Builder interfaceToXml,
    MethodSpec.Builder staticToXml,
    XmlElementWrapperMapper xmlElementWrapperMapper,
    XmlElementsMapper xmlElementsMapper,
    XmlElementMapper xmlElementMapper,
    XmlAttributeMapper xmlAttributeMapper,
    XmlTransientMapper xmlTransientMapper
) {

}
