package io.github.cbarlin.aru.core.impl.visitors;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.OperationType;
import io.github.cbarlin.aru.core.visitors.InterfaceVisitor;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.core.wiring.CorePerInterfaceScope;
import io.github.cbarlin.aru.core.wiring.CorePerRecordScope;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.micronaut.sourcegen.javapoet.AnnotationSpec;
import io.micronaut.sourcegen.javapoet.TypeSpec;

import java.util.List;
import java.util.Optional;

@Component
@CorePerInterfaceScope
public class SuppressWarningsInterfaceVisitor extends InterfaceVisitor {
    private final AdvRecUtilsSettings settings;
    private final TypeSpec.Builder utilsClassBuilder;

    protected SuppressWarningsInterfaceVisitor(final AnalysedInterface analysedInterface) {
        super(new ClaimableOperation("CoreCopySuppressWarnings", OperationType.CLASS), analysedInterface);
        settings = analysedInterface.settings();
        utilsClassBuilder = analysedInterface.utilsClassBuilder();
    }

    @Override
    public int specificity() {
        return 0;
    }

    @Override
    protected boolean visitInterfaceImpl() {
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
