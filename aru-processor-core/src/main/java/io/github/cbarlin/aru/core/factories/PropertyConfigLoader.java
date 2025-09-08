package io.github.cbarlin.aru.core.factories;

import io.avaje.inject.spi.ConfigPropertyPlugin;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class PropertyConfigLoader implements ConfigPropertyPlugin {

    private static final Map<String, Optional<String>> CLAZZ_DETECT = new HashMap<>();
    private static final String FALSE = Boolean.FALSE.toString();
    private static final String TRUE = Boolean.TRUE.toString();

    private final Optional<AdvancedRecordUtilsPrism> optionalPrism;

    public PropertyConfigLoader(@Nullable AdvancedRecordUtilsPrism prism) {
        this(Optional.ofNullable(prism));
    }

    public PropertyConfigLoader(Optional<AdvancedRecordUtilsPrism> prism) {
        this.optionalPrism = prism;
    }

    @Override
    public Optional<String> get(String property) {
        return obtainProperty(property);
    }

    @Override
    public boolean contains(String property) {
        return obtainProperty(property).isPresent();
    }

    @Override
    public boolean equalTo(String property, String value) {
        return obtainProperty(property).filter(s -> Strings.CS.equals(s, value)).isPresent();
    }

    private Optional<String> obtainProperty(final String property) {
        if (property.contains("/") && property.startsWith("class.exists.")) {
            return CLAZZ_DETECT.computeIfAbsent(property, PropertyConfigLoader::checkClass);
        }
        return optionalPrism.map(
            prism -> switch (property) {
                // Default of true means `null` is true, so compare against false
                case "createAdderMethods" -> Boolean.FALSE.equals(prism.builderOptions().createAdderMethods()) ? FALSE : TRUE;
                case "createRemoveMethods" -> Boolean.FALSE.equals(prism.builderOptions().createRemoveMethods()) ? FALSE : TRUE;
                case "createRetainAllMethod" -> Boolean.FALSE.equals(prism.builderOptions().createRetainAllMethod()) ? FALSE : TRUE;
                case "createAllInterface" -> Boolean.FALSE.equals(prism.createAllInterface()) ? FALSE : TRUE;
                case "fluent" -> Boolean.FALSE.equals(prism.builderOptions().fluent()) ? FALSE : TRUE;
                case "concreteSettersForOptional" -> Boolean.FALSE.equals(prism.builderOptions().concreteSettersForOptional()) ? FALSE : TRUE;
                case "nullReplacesNotNull" -> Boolean.FALSE.equals(prism.builderOptions().nullReplacesNotNull()) ? FALSE : TRUE;
                case "buildNullCollectionToEmpty" -> Boolean.FALSE.equals(prism.builderOptions().buildNullCollectionToEmpty()) ? FALSE : TRUE;
                // Default of false means `null` is false, so compare against true
                case "setTimeNowMethods" -> Boolean.TRUE.equals(prism.builderOptions().setTimeNowMethods()) ? TRUE : FALSE;
                case "setToNullMethods" -> Boolean.TRUE.equals(prism.builderOptions().setToNullMethods()) ? TRUE : FALSE;
                case "addJsonbImportAnnotation" -> Boolean.TRUE.equals(prism.addJsonbImportAnnotation()) ? TRUE : FALSE;
                case "diffOptions.staticMethodsAddedToUtils" -> Boolean.TRUE.equals(prism.diffOptions().staticMethodsAddedToUtils()) ? TRUE : FALSE;
                // String(-ish) properties
                case "validatedBuilder" -> Objects.requireNonNullElse(prism.builderOptions().validatedBuilder(), "NONE");
                default -> null;
            }
        );
    }

    private static Optional<String> checkClass(final String property) {
        if (property.startsWith("class.exists.") && property.contains("/")) {
            final String target = Strings.CS.removeStart(property, "class.exists.");
            final String[] split = StringUtils.split(target, '/');
            if (split.length == 2) {
                final ClassName attempt = ClassName.get(split[0], split[1]);
                if (OptionalClassDetector.doesDependencyExist(attempt)) {
                    return Optional.of(TRUE);
                } else {
                    return Optional.of(FALSE);
                }
            }
        }
        return Optional.empty();
    }

}
