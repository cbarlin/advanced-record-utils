package io.github.cbarlin.aru.impl.xml;

import static io.github.cbarlin.aru.impl.Constants.Claims.INTERNAL_MATCHING_IFACE;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.INTERNAL_MATCHING_IFACE_NAME;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;

import io.avaje.spi.ServiceProvider;

@ServiceProvider
public class XmlInterfaceGenerator extends XmlVisitor {

    public XmlInterfaceGenerator() {
        super(Claims.XML_IFACE);
    }

    @Override
    protected int innerSpecificity() {
        return 0;
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected boolean visitStartOfClassImpl(final AnalysedRecord analysedRecord) {
        xmlInterface.builder()
            .addOriginatingElement(analysedRecord.typeElement())
            .addSuperinterface(
                analysedRecord.utilsClassChildInterface(INTERNAL_MATCHING_IFACE_NAME, INTERNAL_MATCHING_IFACE)
                    .className()
            )
            .addJavadoc("Provides the ability for a class to convert itself into XML");
        AnnotationSupplier.addGeneratedAnnotation(xmlInterface, this);
        return true;
    }
}
