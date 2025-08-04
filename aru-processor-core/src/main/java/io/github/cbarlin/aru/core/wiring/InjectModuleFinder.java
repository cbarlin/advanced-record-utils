package io.github.cbarlin.aru.core.wiring;

import java.util.List;

import io.avaje.inject.BeanScope;
import io.avaje.inject.spi.AvajeModule;
import io.avaje.spi.Service;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;

@Service
public interface InjectModuleFinder {

    List<AvajeModule> globalModules(final BeanScope withCoreGlobal);

    List<AvajeModule> recordModules(final BeanScope withCore, final AnalysedRecord analysedRecord);

    List<AvajeModule> interfaceModules(final BeanScope withCore, final AnalysedInterface analysedInterface);

    List<AvajeModule> componentModules(final BeanScope withCore, final AdvancedRecordUtilsPrism prism);
}
