package io.github.cbarlin.aru.core.impl.visitors;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.OperationType;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.core.wiring.CorePerRecordScope;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.micronaut.sourcegen.javapoet.AnnotationSpec;
import io.micronaut.sourcegen.javapoet.TypeSpec;

import java.util.List;
import java.util.Optional;

@Component
@CorePerRecordScope
public class SuppressWarningsRecordVisitor extends RecordVisitor {
    private final AdvRecUtilsSettings settings;
    private final TypeSpec.Builder utilsClassBuilder;

    protected SuppressWarningsRecordVisitor(final AnalysedRecord analysedRecord) {
        super(new ClaimableOperation("CoreCopySuppressWarnings", OperationType.CLASS), analysedRecord);
        settings = analysedRecord.settings();
        utilsClassBuilder = analysedRecord.utilsClassBuilder();
    }

    @Override
    public int specificity() {
        return 0;
    }

    @Override
    protected boolean visitStartOfClassImpl() {
        final List<String> warnings = Optional.ofNullable(settings.prism().addSuppressWarningsAnnotation())
                .map(AdvancedRecordUtilsPrism.SuppressWarningsPrism::value)
                .orElseGet(List::of);
        if (!warnings.isEmpty()) {
            utilsClassBuilder.addAnnotation(AnnotationSpec.get(settings.prism().addSuppressWarningsAnnotation().mirror));
            return true;
        }
        return false;
    }
}
