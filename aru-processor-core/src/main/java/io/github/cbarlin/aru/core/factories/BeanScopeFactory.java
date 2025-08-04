package io.github.cbarlin.aru.core.factories;

import io.avaje.inject.BeanScope;
import io.avaje.inject.BeanScopeBuilder;
import io.avaje.inject.spi.AvajeModule;
import io.github.cbarlin.aru.core.CoreGlobalModule;
import io.github.cbarlin.aru.core.CorePerComponentModule;
import io.github.cbarlin.aru.core.CorePerInterfaceModule;
import io.github.cbarlin.aru.core.CorePerRecordModule;
import io.github.cbarlin.aru.core.impl.ScopeHolder;
import io.github.cbarlin.aru.core.impl.logging.NoOpHandler;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.wiring.InjectModuleFinder;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.RecordComponentElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public final class BeanScopeFactory {

    private static final List<InjectModuleFinder> MODULE_FINDERS;

    static {
        final List<InjectModuleFinder> finders = new ArrayList<>();
        ServiceLoader.load(InjectModuleFinder.class, InjectModuleFinder.class.getClassLoader())
            .iterator()
            .forEachRemaining(finders::add);
        MODULE_FINDERS = List.copyOf(finders);
    }

    private BeanScopeFactory() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static BeanScope loadGlobalScope(final ProcessingEnvironment processingEnvironment) {
        NoOpHandler.install();
        final PropertyConfigLoader propertyConfigLoader = new PropertyConfigLoader(Optional.empty());
        final BeanScopeBuilder coreScopeBuilder = BeanScope.builder();
        coreScopeBuilder.configPlugin(propertyConfigLoader);
        final BeanScope coreScope = coreScopeBuilder
            .bean(ProcessingEnvironment.class, processingEnvironment)
            .modules(new CoreGlobalModule(processingEnvironment))
            .build();

        final List<AvajeModule> otherModules = new ArrayList<>();
        MODULE_FINDERS.forEach(finder -> otherModules.addAll(finder.globalModules(coreScope)));
        if (!otherModules.isEmpty()) {
            final BeanScopeBuilder fullScopeBuilder = BeanScope.builder();
            fullScopeBuilder.configPlugin(propertyConfigLoader);
            return fullScopeBuilder.bean(ProcessingEnvironment.class, processingEnvironment)
                .parent(coreScope, true)
                .modules(otherModules.toArray(AvajeModule[]::new))
                .build();
        } else {
            return coreScope;
        }
    }

    public static ScopeHolder loadRecordScope(final AnalysedRecord analysedRecord, final BeanScope globalScope) {
        final PropertyConfigLoader propertyConfigLoader = new PropertyConfigLoader(analysedRecord.prism());

        final BeanScopeBuilder coreScopeBuilder = BeanScope.builder();
        coreScopeBuilder.configPlugin(propertyConfigLoader);
        final BeanScope coreScope = coreScopeBuilder.parent(globalScope, true)
            .bean(AnalysedRecord.class, analysedRecord)
            .modules(new CorePerRecordModule(analysedRecord))
            .build();

        final List<AvajeModule> otherModules = new ArrayList<>();
        MODULE_FINDERS.forEach(finder -> otherModules.addAll(finder.recordModules(coreScope, analysedRecord)));
        if (!otherModules.isEmpty()) {
            final BeanScopeBuilder fullScopeBuilder = BeanScope.builder();
            fullScopeBuilder.configPlugin(propertyConfigLoader);
            final BeanScope fullScope = fullScopeBuilder.parent(coreScope, true)
                    .bean(AnalysedRecord.class, analysedRecord)
                    .modules(otherModules.toArray(AvajeModule[]::new))
                    .build();
            return new ScopeHolder(fullScope, coreScope);

        } else {
            return new ScopeHolder(coreScope, null);
        }
    }

    public static ScopeHolder loadInterfaceScope(final AnalysedInterface analysedInterface, final BeanScope globalScope) {
        final PropertyConfigLoader propertyConfigLoader = new PropertyConfigLoader(analysedInterface.prism());

        final BeanScopeBuilder coreScopeBuilder = BeanScope.builder();
        coreScopeBuilder.configPlugin(propertyConfigLoader);
        final BeanScope coreScope = coreScopeBuilder.parent(globalScope, true)
            .bean(AnalysedInterface.class, analysedInterface)
            .modules(new CorePerInterfaceModule(analysedInterface))
            .build();


        final List<AvajeModule> otherModules = new ArrayList<>();
        MODULE_FINDERS.forEach(finder -> otherModules.addAll(finder.interfaceModules(coreScope, analysedInterface)));
        if (!otherModules.isEmpty()) {
            final BeanScopeBuilder fullScopeBuilder = BeanScope.builder();
            fullScopeBuilder.configPlugin(propertyConfigLoader);
            final BeanScope fullScope = fullScopeBuilder.parent(coreScope, true)
                    .bean(AnalysedInterface.class, analysedInterface)
                    .modules(otherModules.toArray(AvajeModule[]::new))
                    .build();
            return new ScopeHolder(fullScope, coreScope);
        } else {
            return new ScopeHolder(coreScope, null);
        }
    }

    public static ScopeHolder loadComponentScope(final RecordComponentElement rce, final BeanScope globalScope, final AnalysedRecord analysedRecord) {
        final PropertyConfigLoader propertyConfigLoader = new PropertyConfigLoader(analysedRecord.prism());

        final BeanScopeBuilder coreScopeBuilder = BeanScope.builder();
        coreScopeBuilder.configPlugin(propertyConfigLoader);
        final BeanScope coreScope = coreScopeBuilder.parent(globalScope, true)
            .bean(RecordComponentElement.class, rce)
            .modules(new CorePerComponentModule(rce))
            .build();

        final List<AvajeModule> otherModules = new ArrayList<>();
        MODULE_FINDERS.forEach(finder -> otherModules.addAll(finder.componentModules(coreScope, analysedRecord.prism())));
        if (!otherModules.isEmpty()) {
            final BeanScopeBuilder fullScopeBuilder = BeanScope.builder();
            fullScopeBuilder.configPlugin(propertyConfigLoader);
            final BeanScope fullScope = fullScopeBuilder.parent(coreScope, true)
                    .bean(RecordComponentElement.class, rce)
                    .modules(otherModules.toArray(AvajeModule[]::new))
                    .build();
            return new ScopeHolder(fullScope, coreScope);
        } else {
            return new ScopeHolder(coreScope, null);
        }
    }

}
