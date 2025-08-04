package io.github.cbarlin.aru.core.impl.visitors;

import static io.github.cbarlin.aru.core.CommonsConstants.JDOC_PARA;

import javax.lang.model.element.Modifier;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.core.wiring.CorePerRecordScope;

@Component
@CorePerRecordScope
public final class BuilderClassCreatorVisitor extends RecordVisitor {
    private final BuilderClass builder;

    public BuilderClassCreatorVisitor(
        final AnalysedRecord analysedRecord, 
        final BuilderClass builderClass
    ) {
        super(CommonsConstants.Claims.CORE_BUILDER_CLASS, analysedRecord);
        this.builder = builderClass;
    }

    // Since this creates the builder, it needs to be first
    @Override
    public int specificity() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected boolean visitStartOfClassImpl() {
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
