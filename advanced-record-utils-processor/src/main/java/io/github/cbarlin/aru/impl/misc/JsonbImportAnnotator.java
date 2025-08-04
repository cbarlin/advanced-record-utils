package io.github.cbarlin.aru.impl.misc;

import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.Constants.Names;
import io.github.cbarlin.aru.impl.wiring.BasePerRecordScope;
import io.github.cbarlin.aru.prism.prison.JsonBPrism;
import io.micronaut.sourcegen.javapoet.AnnotationSpec;
import jakarta.inject.Singleton;

@Singleton
@BasePerRecordScope
@RequiresProperty(value = "addJsonbImportAnnotation", equalTo = "true")
public final class JsonbImportAnnotator extends RecordVisitor {

    public JsonbImportAnnotator(final AnalysedRecord analysedRecord) {
        super(Claims.MISC_AVAJE_JSONB_IMPORT, analysedRecord);
    }

    @Override
    public int specificity() {
        return 0;
    }

    @Override
    protected boolean visitStartOfClassImpl() {
        if (JsonBPrism.isPresent(analysedRecord.typeElement())) {
            // Already done
            return true;
        }
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
