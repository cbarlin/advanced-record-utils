package io.github.cbarlin.aru.impl.merger;

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
public final class MergerInterfaceGenerator extends RecordVisitor {

    private static final String NEW_JAVADOC_LINE = "\n<p>\n";

    public MergerInterfaceGenerator() {
        super(Claims.MERGER_IFACE);
    }

    @Override
    public int specificity() {
        return Integer.MAX_VALUE - 2;
    }

    @Override
    public boolean isApplicable(final AnalysedRecord analysedRecord) {
        return Boolean.TRUE.equals(analysedRecord.settings().prism().merger());
    }

    @Override
    protected boolean visitStartOfClassImpl(final AnalysedRecord analysedRecord) {
        if (!analysedRecord.isClaimed(io.github.cbarlin.aru.core.CommonsConstants.Claims.CORE_BUILDER_CLASS)) {
            APContext.messager().printError("The builder wasn't claimed before the Merger!");
        }
        final String generatedName = analysedRecord.settings().prism().mergerOptions().mergerName();
        final var builder = analysedRecord.utilsClassChildInterface(generatedName, claimableOperation);
        AnnotationSupplier.addGeneratedAnnotation(builder, this);
        builder.builder()
            .addAnnotation(CommonsConstants.Names.NULL_MARKED)
            .addOriginatingElement(analysedRecord.typeElement())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addJavadoc("Interface for a record that can be merged with itself.")
            .addJavadoc(NEW_JAVADOC_LINE)
            .addJavadoc("Intended merge process is that, for each field:")
            .addJavadoc("\n<ol>\n")
            .addJavadoc("<li>If both of the two instances have a null value, then the result is null</li>\n")
            .addJavadoc("<li>If one of the two instances has a null value, then take the non-null value</li>\n")
            .addJavadoc("<li>If both are non-null, and the field is itself can be merged, then merge the values using the other merger</li>\n")
            .addJavadoc("<li>If both are non-null, and the field is a collection, then union the collections</li>\n")
            .addJavadoc("<li>Otherwise, keep the value in this instance (instead of the one in the other instance)</li>\n")
            .addJavadoc("</ol>\n")
            .addSuperinterface(
                analysedRecord.utilsClassChildInterface(INTERNAL_MATCHING_IFACE_NAME, INTERNAL_MATCHING_IFACE)
                    .className()
            );
        return true;
    }
}
