package io.github.cbarlin.aru.impl.xml.utils.elements.noncollections;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.artifacts.PreBuilt;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.types.components.AnalysedOptionalComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.ComponentTargetingInterface;
import io.github.cbarlin.aru.impl.wiring.XmlPerComponentScope;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementsPrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_DEFAULT_STRING;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_UTILS_CLASS;
import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_ELEMENT_DEFAULT;

@Singleton
@XmlPerComponentScope
@RequiresBean({XmlElementsPrism.class, ComponentTargetingInterface.class})
public final class Branching extends NonCollectionXmlVisitor {

    private final XmlElementsPrism outerPrism;
    private final AnalysedInterface other;

    public Branching (
        final XmlRecordHolder xmlRecordHolder,
        final XmlElementsPrism prism,
        final ComponentTargetingInterface component,
        final Optional<AnalysedOptionalComponent> analysedOptionalComponent
    ) {
        super(Claims.XML_WRITE_FIELD, xmlRecordHolder, analysedOptionalComponent);
        this.outerPrism = prism;
        this.other = component.target();
    }

    @Override
    protected int innerSpecificity() {
        return 3;
    }

    @Override
    protected boolean writeElementMethod(final AnalysedComponent analysedComponent) {
        final MethodSpec.Builder methodBuilder = createMethod(analysedComponent, analysedComponent.serialisedTypeName());

        final List<ProcessingTarget> targets = other.implementingTypes()
            .stream()
            .filter(this::concreteImplementingType)
            .toList();
        final Map<ClassName, NameAndNamespace> extractedName = extractNamedVersions(analysedComponent, other);
        
        methodBuilder.beginControlFlow("if ($T.isNull(val))", OBJECTS)
            .addStatement("return");

        for (final ProcessingTarget target : targets) {
            final ClassName otherUtilsClassName = getOtherXmlUtils(target);
            if (Objects.isNull(otherUtilsClassName)) {
                continue;
            }
            final ClassName targetClassName = ClassName.get(target.typeElement());
            methodBuilder.nextControlFlow("else if (val instanceof $T tVal)", targetClassName);
            final String name = Optional.ofNullable(extractedName.get(targetClassName))
                .flatMap(NameAndNamespace::name)
                .orElseGet(() -> elementName(target.typeElement()));
            final Optional<String> namespaceName = Optional.ofNullable(extractedName.get(targetClassName))
                .flatMap(NameAndNamespace::namespace);
        
            namespaceName.ifPresentOrElse(
                namespace -> methodBuilder.addStatement(
                    "$T.$L(tVal, output, $S, $S, currentDefaultNamespace)", 
                    otherUtilsClassName,
                    STATIC_WRITE_XML_NAME,
                    name,
                    namespace
                ),
                () -> methodBuilder.addStatement(
                    "$T.$L(tVal, output, $S, null, currentDefaultNamespace)", 
                    otherUtilsClassName,
                    STATIC_WRITE_XML_NAME,
                    name
                )
            );
        }

        methodBuilder.endControlFlow();

        return true;
    }

    @Nullable
    final ClassName getOtherXmlUtils(final ProcessingTarget target) {
        if (target instanceof final AnalysedRecord analysedRecord) {
            return analysedRecord.utilsClass().childClassArtifact(XML_UTILS_CLASS, Claims.XML_STATIC_CLASS).className();
        } else if (target instanceof final LibraryLoadedTarget libraryLoadedTarget) {
            return Optional.ofNullable(libraryLoadedTarget.utilsClass().childArtifact(XML_UTILS_CLASS, Claims.XML_STATIC_CLASS))
                .map(PreBuilt::className)
                .orElse(null);
        }
        return null;
    }

    private Map<ClassName, NameAndNamespace> extractNamedVersions(final AnalysedComponent analysedComponent, final AnalysedInterface other) {
        final Map<ClassName, NameAndNamespace> extracted = HashMap.newHashMap(other.implementingTypes().size());
        for (final XmlElementPrism prism : outerPrism.value()) {
            final ClassName className = ClassName.get(APContext.asTypeElement(prism.type()));
            if (className.equals(XML_ELEMENT_DEFAULT)) {
                APContext.messager().printError("XmlElement inside XmlElements must have a target type", analysedComponent.element());
            } else {
                final Optional<String> name = Optional.ofNullable(prism.name())
                    .filter(StringUtils::isNotBlank)
                    .filter(Predicate.not(XML_DEFAULT_STRING::equals));
                final Optional<String> namespace = Optional.ofNullable(prism.namespace())
                    .filter(StringUtils::isNotBlank)
                    .filter(Predicate.not(XML_DEFAULT_STRING::equals));
                extracted.put(className, new NameAndNamespace(name, namespace));
            }
        }
        return Map.copyOf(extracted);
    }

    private boolean concreteImplementingType (final ProcessingTarget processingTarget) {
        return (processingTarget instanceof AnalysedRecord) || 
            (processingTarget instanceof LibraryLoadedTarget);
    }

    private record NameAndNamespace(Optional<String> name, Optional<String> namespace) {}
}
