package io.github.cbarlin.aru.core.analysers;

import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Element;
import java.util.Optional;

public sealed interface TargetAnalyser permits ConcreteTargetAnalyser, ImportAnalyser, LibraryLoadedTargetAnalyser, PackageTargetAnalyser, TypeConverterAnalyser {
    
    TargetAnalysisResult analyse(final Element element, final Optional<AdvRecUtilsSettings> parentSettings);

    default TargetAnalysisResult analyse(final Element element) {
        return analyse(element, Optional.empty());
    }

    @Nullable
    public static AdvRecUtilsSettings finaliseSettings(final Optional<AdvRecUtilsSettings> fromElement, final Optional<AdvRecUtilsSettings> fromParent) {
        if (fromElement.isPresent() && fromParent.isPresent()) {
            // Prefer the current elements settings over the parent's ones
            return AdvRecUtilsSettings.merge(fromElement.get(), fromParent.get());
        } else if (fromParent.isPresent()) {
            return fromParent.get();
        } else if (fromElement.isPresent()) {
            return fromElement.get();
        }
        return null;
    }
}
