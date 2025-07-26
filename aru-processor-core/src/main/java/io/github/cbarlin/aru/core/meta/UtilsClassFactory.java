package io.github.cbarlin.aru.core.meta;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import io.avaje.inject.Profile;
import io.github.cbarlin.aru.core.artifacts.UtilsClass;
import io.github.cbarlin.aru.core.types.AnalysedType;

@Factory
public final class UtilsClassFactory {

    @Bean
    @BeanTypes(UtilsClass.class)
    @Profile({"aru-reset-per-record", "aru-reset-per-interface"})
    UtilsClass utilsClass(final AnalysedType analysedType) {
        return new UtilsClass(analysedType.utilsClass());
    }
}
