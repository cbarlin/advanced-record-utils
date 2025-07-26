package io.github.cbarlin.aru.core.analysers;

import java.util.Optional;

import javax.lang.model.element.Element;

import org.jspecify.annotations.Nullable;

import io.github.cbarlin.aru.core.AdvRecUtilsSettings;

public sealed interface TargetAnalyser permits LibraryLoadedTargetAnalyser, PackageTargetAnalyser, ConcreteTargetAnalyser {
    
    TargetAnalysisResult analyse(final Element element, final Optional<AdvRecUtilsSettings> parentSettings);

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
