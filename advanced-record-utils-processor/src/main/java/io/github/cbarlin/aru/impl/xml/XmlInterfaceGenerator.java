package io.github.cbarlin.aru.impl.xml;

import static io.github.cbarlin.aru.impl.Constants.Claims.INTERNAL_MATCHING_IFACE;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.INTERNAL_MATCHING_IFACE_NAME;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.XmlPerRecordScope;
import jakarta.inject.Singleton;

@Singleton
@XmlPerRecordScope
public final class XmlInterfaceGenerator extends XmlVisitor {

    public XmlInterfaceGenerator(final XmlRecordHolder xmlRecordHolder) {
        super(Claims.XML_IFACE, xmlRecordHolder);
    }

    @Override
    protected int innerSpecificity() {
        return 0;
    }

    @Override
    protected boolean visitStartOfClassImpl() {
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
