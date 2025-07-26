package io.github.cbarlin.aru.core.analysers;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import io.avaje.inject.Component;
import io.avaje.inject.Priority;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.wiring.AruGlobal;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;

@Component
@AruGlobal
@Priority(3)
public final class PackageTargetAnalyser implements TargetAnalyser {

    @Override
    public TargetAnalysisResult analyse(final Element element, final Optional<AdvRecUtilsSettings> parentSettings) {
        if ((!(element instanceof final PackageElement packageElement)) || (!AdvancedRecordUtilsPrism.isPresent(packageElement))) {
            return TargetAnalysisResult.EMPTY_RESULT;
        }
        
        final AdvancedRecordUtilsPrism prism = AdvancedRecordUtilsPrism.getInstanceOn(packageElement);
        if (!Boolean.TRUE.equals(prism.applyToAllInPackage())) {
            return TargetAnalysisResult.EMPTY_RESULT;
        }

        // Excellent, all the items in this package are possible targets! Exciting!
        final Set<TypeElement> typeElements = new HashSet<>();
        
        for (final Element el : packageElement.getEnclosedElements()) {
            if (el instanceof final TypeElement ty) {
                typeElements.add(ty);
            }
        }

        return new TargetAnalysisResult(Optional.empty(), typeElements, true);
    }
}
