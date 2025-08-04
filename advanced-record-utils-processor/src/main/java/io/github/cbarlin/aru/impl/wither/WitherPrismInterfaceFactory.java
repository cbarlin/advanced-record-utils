package io.github.cbarlin.aru.impl.wither;

import javax.lang.model.element.Modifier;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.misc.MatchingInterface;
import io.github.cbarlin.aru.impl.wiring.WitherPerRecordScope;
import io.github.cbarlin.aru.prism.prison.WitherOptionsPrism;

/**
 * We can assume the "WitherPerRecordModule" isn't wired if withers are disabled
 */
@Factory
@WitherPerRecordScope
public final class WitherPrismInterfaceFactory {

    private final AnalysedRecord analysedRecord;

    public WitherPrismInterfaceFactory(final AnalysedRecord analysedRecord) {
        this.analysedRecord = analysedRecord;
    }

    @Bean
    WitherOptionsPrism witherOptionsPrism() {
        return analysedRecord.prism().witherOptions();
    }

    @Bean
    @BeanTypes(WitherInterface.class)
    WitherInterface witherInterface(final WitherOptionsPrism witherOptionsPrism, final MatchingInterface matchingInterface) {
        final String generatedName = witherOptionsPrism.witherName();
        final ToBeBuilt builder = analysedRecord.utilsClassChildInterface(generatedName, Claims.WITHER_IFACE);
        AnnotationSupplier.addGeneratedAnnotation(builder, WitherPrismInterfaceFactory.class, Claims.WITHER_IFACE);
        builder.builder()
            .addAnnotation(CommonsConstants.Names.NULL_MARKED)
            .addModifiers(Modifier.STATIC)
            .addJavadoc("An interface that provides the ability to create new instances of a record with modifications")
            .addSuperinterface(matchingInterface.className());
        return new WitherInterface(builder);
    }
}
