package io.github.cbarlin.aru.core.analysers;

import io.avaje.inject.Component;
import io.avaje.inject.Priority;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.wiring.CoreGlobalScope;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
@CoreGlobalScope
@Priority(3)
public final class PackageTargetAnalyser implements TargetAnalyser {

    @Override
    public TargetAnalysisResult analyse(final Element element, final Optional<AdvRecUtilsSettings> parentSettings) {
        if ((!(element instanceof final PackageElement packageElement)) || (!AdvancedRecordUtilsPrism.isPresent(packageElement))) {
            return TargetAnalysisResult.EMPTY_RESULT;
        }
        
        final AdvancedRecordUtilsPrism prism = AdvancedRecordUtilsPrism.getInstanceOn(packageElement);
        if (Objects.isNull(prism) || !Boolean.TRUE.equals(prism.applyToAllInPackage())) {
            return TargetAnalysisResult.EMPTY_RESULT;
        }

        // Excellent, all the items in this package are possible targets! Exciting!
        final Set<TypeElement> typeElements = new HashSet<>();
        
        for (final Element el : packageElement.getEnclosedElements()) {
            if (el instanceof final TypeElement ty) {
                typeElements.add(ty);
            }
        }

        AdvancedRecordUtilsPrism.getOptionalOn(packageElement)
                .map(AdvancedRecordUtilsPrism::importTargets)
                .orElse(List.of())
                .stream()
                .map(APContext.types()::asElement)
                .filter(Objects::nonNull)
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .forEach(typeElements::add);

        return new TargetAnalysisResult(Optional.empty(), typeElements, true, Optional.empty());
    }
}
