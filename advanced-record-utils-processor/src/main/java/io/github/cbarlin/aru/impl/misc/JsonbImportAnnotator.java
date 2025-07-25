package io.github.cbarlin.aru.impl.misc;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.Constants.Names;
import io.micronaut.sourcegen.javapoet.AnnotationSpec;

@ServiceProvider
public final class JsonbImportAnnotator extends RecordVisitor {

    public JsonbImportAnnotator() {
        super(Claims.MISC_AVAJE_JSONB_IMPORT);
    }

    @Override
    public int specificity() {
        return 0;
    }

    @Override
    public boolean isApplicable(final AnalysedRecord analysedRecord) {
        return Boolean.TRUE.equals(analysedRecord.settings().prism().addJsonbImportAnnotation());
    }

    @Override
    protected boolean visitStartOfClassImpl(final AnalysedRecord analysedRecord) {
        if (!analysedRecord.intendedType().equals(analysedRecord.className())) {
            analysedRecord.utilsClass()
            .builder()
            .addAnnotation(
                AnnotationSpec.builder(Names.AVAJE_JSONB_IMPORT)
                    .addMember("value", "{$T.class, $T.class}", analysedRecord.className(), analysedRecord.intendedType())
                    .build()
            );
        } else {
            analysedRecord.utilsClass()
            .builder()
            .addAnnotation(
                AnnotationSpec.builder(Names.AVAJE_JSONB_IMPORT)
                    .addMember("value", "{$T.class}", analysedRecord.className())
                    .build()
            );
        }
        
        return true;
    }
}
