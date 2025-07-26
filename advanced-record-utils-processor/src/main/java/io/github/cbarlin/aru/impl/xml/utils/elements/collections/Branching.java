package io.github.cbarlin.aru.impl.xml.utils.elements.collections;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.artifacts.PreBuilt;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedOptionalComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.ComponentTargetingInterface;
import io.github.cbarlin.aru.impl.wiring.XmlPerComponentScope;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementWrapperPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementsPrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;
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
@RequiresBean({XmlElementsPrism.class, ComponentTargetingInterface.class, AnalysedCollectionComponent.class})
public final class Branching extends XmlVisitor {

    private final XmlElementsPrism outerPrism;
    private final Optional<XmlElementWrapperPrism> wrapper;
    private final AnalysedCollectionComponent component;
    private final AnalysedInterface other;
    private final Optional<AnalysedOptionalComponent> optComponent;

    public Branching (
        final XmlRecordHolder xmlRecordHolder,
        final XmlElementsPrism prism,
        final Optional<XmlElementWrapperPrism> wrapper,
        final AnalysedCollectionComponent component,
        final ComponentTargetingInterface targetingRecord,
        final Optional<AnalysedOptionalComponent> optComponent
    ) {
        super(Claims.XML_WRITE_FIELD, xmlRecordHolder);
        this.outerPrism = prism;
        this.wrapper = wrapper;
        this.component = component;
        this.other = targetingRecord.target();
        this.optComponent = optComponent;
    }

    @Override
    protected int innerSpecificity() {
        return 4;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final MethodSpec.Builder methodBuilder = createMethod(component, component.typeName());
        final List<ProcessingTarget> targets = other.implementingTypes()
            .stream()
            .filter(this::concreteImplementingType)
            .toList();
        final Map<ClassName, NameAndNamespace> extractedName = extractNamedVersions(component, other);

        methodBuilder.beginControlFlow("if ($T.isNull(val))", OBJECTS)
            .addStatement("return")
            .endControlFlow();

        wrapper.ifPresent(xmlElementWrapperPrism -> writeWrapperElement(methodBuilder, xmlElementWrapperPrism));

        component.withinUnwrapped(
            variableName -> {
                methodBuilder.beginControlFlow("if($T.isNull($L))", OBJECTS, variableName)
                    .addStatement("continue");
                for (final ProcessingTarget target : targets) {
                    writeTargetIfStatement(methodBuilder, extractedName, target, variableName);
                }
                methodBuilder.endControlFlow();
            },
            methodBuilder,
            "val",
            TypeName.get(other.typeMirror())
        );

        if (wrapper.isPresent()) {
            methodBuilder.addStatement("output.writeEndElement()");
            if (!Boolean.TRUE.equals(xmlOptionsPrism.writeEmptyCollectionsWithWrapperAsEmptyElement())) {
                methodBuilder.endControlFlow();
            }
        }

        return true;
    }

    private void writeWrapperElement(final MethodSpec.Builder methodBuilder, final XmlElementWrapperPrism wrapperPrism) {
        if (!Boolean.TRUE.equals(xmlOptionsPrism.writeEmptyCollectionsWithWrapperAsEmptyElement())) {
            methodBuilder.beginControlFlow("if (!val.isEmpty())");
        }
        Optional.ofNullable(wrapperPrism.namespace())
            .filter(StringUtils::isNotBlank)
            .filter(Predicate.not(XML_DEFAULT_STRING::equals))
            .ifPresentOrElse(
                namespace -> methodBuilder.addStatement("output.writeStartElement($S, $S)", namespace, wrapperPrism.name()),
                () -> methodBuilder.addStatement("output.writeStartElement($S)", wrapperPrism.name())
            );
    }

    private void writeTargetIfStatement(final MethodSpec.Builder methodBuilder, final Map<ClassName, NameAndNamespace> extractedName, final ProcessingTarget target, final String varName) {
        final ClassName otherUtilsClassName = getOtherXmlUtils(target);
        if (Objects.isNull(otherUtilsClassName)) {
            return;
        }
        final ClassName targetClassName = ClassName.get(target.typeElement());
        methodBuilder.nextControlFlow("else if ($L instanceof $T tVal)", varName, targetClassName);
        final String name = Optional.ofNullable(extractedName.get(targetClassName))
            .flatMap(NameAndNamespace::name)
            .orElseGet(() -> elementName(target.typeElement()));
        final Optional<String> namespaceName = Optional.ofNullable(extractedName.get(targetClassName))
            .flatMap(NameAndNamespace::namespace)
            .or(() -> namespaceName(target.typeElement()));
         
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
