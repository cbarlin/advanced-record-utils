package io.github.cbarlin.aru.impl.wither;

import static io.github.cbarlin.aru.impl.Constants.Claims.INTERNAL_MATCHING_IFACE;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.INTERNAL_MATCHING_IFACE_NAME;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;

@ServiceProvider
public final class WitherInterfaceGenerator extends RecordVisitor  {

    public WitherInterfaceGenerator() {
        super(Claims.WITHER_IFACE);
    }

    // Since this creates the wither, it should be close to first
    @Override
    public int specificity() {
        return Integer.MAX_VALUE - 2;
    }

    @Override
    public boolean isApplicable(final AnalysedRecord analysedRecord) {
        return analysedRecord.settings().prism().wither();
    }

    @Override
    protected boolean visitStartOfClassImpl(final AnalysedRecord analysedRecord) {
        if (!analysedRecord.isClaimed(io.github.cbarlin.aru.core.CommonsConstants.Claims.CORE_BUILDER_CLASS)) {
            APContext.messager().printError("The builder wasn't claimed before the Wither!");
        }
        final String generatedName = analysedRecord.settings().prism().witherOptions().witherName();
        final var builder = analysedRecord.utilsClassChildInterface(generatedName, claimableOperation);
        AnnotationSupplier.addGeneratedAnnotation(builder, this);
        builder.builder()
            .addAnnotation(CommonsConstants.Names.NULL_MARKED)
            .addModifiers(Modifier.STATIC)
            .addJavadoc("An interface that provides the ability to create new instances of a record with modifications")
            .addSuperinterface(
                analysedRecord.utilsClassChildInterface(INTERNAL_MATCHING_IFACE_NAME, INTERNAL_MATCHING_IFACE)
                    .className()
            );
        return true;
    }
}
