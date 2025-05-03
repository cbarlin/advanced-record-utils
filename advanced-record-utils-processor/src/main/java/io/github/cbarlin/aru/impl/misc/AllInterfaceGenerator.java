package io.github.cbarlin.aru.impl.misc;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;

import io.avaje.spi.ServiceProvider;

@ServiceProvider
public class AllInterfaceGenerator extends RecordVisitor  {

    public AllInterfaceGenerator() {
        super(Claims.ALL_IFACE);
    }

    // Since this creates the "all" interface, it should be last
    @Override
    public int specificity() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean isApplicable(final AnalysedRecord analysedRecord) {
        return analysedRecord.settings().prism().createAllInterface();
    }

    @Override
    protected boolean visitStartOfClassImpl(final AnalysedRecord analysedRecord) {
        final String generatedName = "All";
        final var builder = analysedRecord.utilsClassChildInterface(generatedName, claimableOperation);
        AnnotationSupplier.addGeneratedAnnotation(builder, this);
        builder.builder().addAnnotation(CommonsConstants.Names.NULL_MARKED);

        analysedRecord.utilsClass().visitChildArtifacts(artifact -> {
            if (artifact != builder && artifact instanceof final ToBeBuiltInterface tbbi) {
                builder.builder().addSuperinterface(tbbi.className());
            }
        });

        return true;
    }
}
