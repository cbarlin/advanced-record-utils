package io.github.cbarlin.aru.impl.wiring;

import io.avaje.inject.BeanScope;
import io.avaje.inject.spi.AvajeModule;
import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.wiring.InjectModuleFinder;
import io.github.cbarlin.aru.impl.BasePerComponentModule;
import io.github.cbarlin.aru.impl.BasePerInterfaceModule;
import io.github.cbarlin.aru.impl.BasePerRecordModule;
import io.github.cbarlin.aru.impl.GlobalModule;
import io.github.cbarlin.aru.impl.builder.BuilderPerComponentModule;
import io.github.cbarlin.aru.impl.builder.BuilderPerInterfaceModule;
import io.github.cbarlin.aru.impl.builder.validation.BuilderPerRecordModule;
import io.github.cbarlin.aru.impl.diff.DiffPerComponentModule;
import io.github.cbarlin.aru.impl.diff.DiffPerRecordModule;
import io.github.cbarlin.aru.impl.merger.MergerPerRecordModule;
import io.github.cbarlin.aru.impl.merger.utils.MergerPerComponentModule;
import io.github.cbarlin.aru.impl.wither.WitherPerComponentModule;
import io.github.cbarlin.aru.impl.wither.WitherPerRecordModule;
import io.github.cbarlin.aru.impl.xml.XmlPerComponentModule;
import io.github.cbarlin.aru.impl.xml.XmlPerRecordModule;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;

import java.util.ArrayList;
import java.util.List;

@ServiceProvider
public final class InjectModuleProvider implements InjectModuleFinder {

    @Override
    public List<AvajeModule> globalModules(final BeanScope withCoreGlobal) {
        final List<AvajeModule> modules = new ArrayList<>(1);
        modules.add(new GlobalModule());
        return modules;
    }

    @Override
    public List<AvajeModule> recordModules(final BeanScope withCore, final AnalysedRecord analysedRecord) {
        final List<AvajeModule> modules = new ArrayList<>(5);
        modules.add(new BasePerRecordModule());
        modules.add(new BuilderPerRecordModule());

        final AdvancedRecordUtilsPrism prism = analysedRecord.prism();
        
        if (!Boolean.FALSE.equals(prism.wither())) {
            modules.add(new WitherPerRecordModule());
        }

        if (Boolean.TRUE.equals(prism.merger())) {
            modules.add(new MergerPerRecordModule());
        }

        if (Boolean.TRUE.equals(prism.diffable())) {
            modules.add(new DiffPerRecordModule());
        }

        if (Boolean.TRUE.equals(prism.xmlable())) {
            modules.add(new XmlPerRecordModule());
        }

        return modules;
    }

    @Override
    public List<AvajeModule> interfaceModules(final BeanScope withCore, final AnalysedInterface analysedInterface) {
        final List<AvajeModule> modules = new ArrayList<>(2);
        modules.add(new BasePerInterfaceModule());
        modules.add(new BuilderPerInterfaceModule());
        return modules;
    }

    @Override
    public List<AvajeModule> componentModules(final BeanScope withCore, final AdvancedRecordUtilsPrism prism) {
        final List<AvajeModule> modules = new ArrayList<>(5);
        modules.add(new BasePerComponentModule());
        modules.add(new BuilderPerComponentModule());

        if (!Boolean.FALSE.equals(prism.wither())) {
            modules.add(new WitherPerComponentModule());
        }

        if (Boolean.TRUE.equals(prism.merger())) {
            modules.add(new MergerPerComponentModule());
        }

        if (Boolean.TRUE.equals(prism.diffable())) {
            modules.add(new DiffPerComponentModule());
        }

        if (Boolean.TRUE.equals(prism.xmlable())) {
            modules.add(new XmlPerComponentModule());
        }

        return modules;
    }

}
