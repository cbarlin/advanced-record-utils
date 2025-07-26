package io.github.cbarlin.aru.impl.misc;

import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.INTERNAL_MATCHING_IFACE_NAME;

import javax.lang.model.element.Modifier;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.BasePerRecordScope;

@Factory
@BasePerRecordScope
public final class MatchingInterfaceFactory {

    private final AnalysedRecord analysedRecord;

    public MatchingInterfaceFactory(AnalysedRecord analysedRecord) {
        this.analysedRecord = analysedRecord;
    }

    @Bean
    @BeanTypes(MatchingInterface.class)
    MatchingInterface matchingInterface() {
        final MatchingInterface m = new MatchingInterface(analysedRecord.utilsClassChildInterface(INTERNAL_MATCHING_IFACE_NAME, Claims.INTERNAL_MATCHING_IFACE), analysedRecord);
        AnnotationSupplier.addGeneratedAnnotation(m, MatchingInterfaceFactory.class, m.claimableOperation());
        m.builder()
            .addAnnotation(CommonsConstants.Names.NULL_UNMARKED)
            .addModifiers(Modifier.STATIC);
        return m;
    }
}
