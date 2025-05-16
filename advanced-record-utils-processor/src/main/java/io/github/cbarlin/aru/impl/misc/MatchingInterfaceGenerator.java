package io.github.cbarlin.aru.impl.misc;

import javax.lang.model.element.Modifier;

import org.jspecify.annotations.Nullable;

import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.*;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;

import io.avaje.spi.ServiceProvider;

@ServiceProvider
public class MatchingInterfaceGenerator extends RecordVisitor {

    @Nullable
    private ToBeBuilt allerBuilder;

    public MatchingInterfaceGenerator() {
        super(Claims.INTERNAL_MATCHING_IFACE);
    }

    @Override
    public int specificity() {
        return Integer.MAX_VALUE - 1;
    }

    @Override
    public boolean isApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected boolean visitStartOfClassImpl(final AnalysedRecord analysedRecord) {
        allerBuilder = analysedRecord.utilsClassChildInterface(INTERNAL_MATCHING_IFACE_NAME, claimableOperation);
        AnnotationSupplier.addGeneratedAnnotation(allerBuilder, this);
        allerBuilder.builder()
            .addAnnotation(CommonsConstants.Names.NULL_UNMARKED)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        return true;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final var methodBuilder = allerBuilder.createMethod(analysedComponent.name(), claimableOperation, analysedComponent)
            .returns(analysedComponent.typeName())
            .addModifiers(Modifier.ABSTRACT);

        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        return true;
    }
}
