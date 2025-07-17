package io.github.cbarlin.aru.core.impl.visitors;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import static io.github.cbarlin.aru.core.CommonsConstants.JDOC_PARA;

import io.avaje.spi.ServiceProvider;

@ServiceProvider
public class BuilderClassCreatorVisitor extends RecordVisitor {

    public BuilderClassCreatorVisitor() {
        super(CommonsConstants.Claims.CORE_BUILDER_CLASS);
    }

    // Since this creates the builder, it needs to be first
    @Override
    public int specificity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isApplicable(AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected boolean visitStartOfClassImpl(AnalysedRecord analysedRecord) {
        final String generatedName = analysedRecord.settings().prism().builderOptions().builderName();
        final var builder = analysedRecord.utilsClassChildClass(generatedName, claimableOperation);
        AnnotationSupplier.addGeneratedAnnotation(builder, this);
        builder.builder().addAnnotation(CommonsConstants.Names.NULL_MARKED)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .addJavadoc("A class used for building {@link $T} objects", analysedRecord.intendedType());
        if (!analysedRecord.intendedType().equals(analysedRecord.className())) {
            builder.builder().addJavadoc(JDOC_PARA)
                .addJavadoc("Generates {@link $T} objects using the {@link $T} implementation objects", analysedRecord.intendedType(), analysedRecord.className());
        }
        return true;
    }
}
