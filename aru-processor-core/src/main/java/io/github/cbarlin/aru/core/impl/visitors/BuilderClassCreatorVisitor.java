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
import io.github.cbarlin.aru.prism.prison.IncludeJFRPrism;
import io.micronaut.sourcegen.javapoet.FieldSpec;

import java.util.Objects;

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
        if (shouldCreateJfr()) {
            final FieldSpec jfr = FieldSpec.builder(CommonsConstants.Names.ARU_JFR_BUILDER, "__aruJfrEvent", Modifier.PRIVATE, Modifier.FINAL)
                    .initializer("new $T($T.class, $T.class)", CommonsConstants.Names.ARU_JFR_BUILDER, analysedRecord.utilsClassName(), analysedRecord.className())
                    .build();
            builder.builder().addField(jfr);
        }
        return true;
    }

    private boolean shouldCreateJfr() {
        if (IncludeJFRPrism.isPresent(analysedRecord.typeElement())) {
            final IncludeJFRPrism prism = Objects.requireNonNull(IncludeJFRPrism.getInstanceOn(analysedRecord.typeElement()));
            return !Boolean.FALSE.equals(prism.builder());
        }
        return false;
    }
}
