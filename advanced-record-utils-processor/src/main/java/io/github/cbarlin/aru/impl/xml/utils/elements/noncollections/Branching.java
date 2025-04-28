package io.github.cbarlin.aru.impl.xml.utils.elements.noncollections;

import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_DEFAULT_STRING;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.XML_UTILS_CLASS;
import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_ELEMENT_DEFAULT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.artifacts.PreBuilt;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementsPrism;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public class Branching extends XmlVisitor {

    public Branching() {
        super(Claims.XML_WRITE_FIELD);
    }

    @Override
    protected int innerSpecificity() {
        return 1;
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final Optional<AnalysedInterface> optionalSupported = extractSupported(analysedComponent);
        if (optionalSupported.isPresent()) {
            final MethodSpec.Builder methodBuilder = createMethod(analysedComponent, analysedComponent.typeName());
            
            final AnalysedInterface other = optionalSupported.get();
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
        return false;
    }

    // False positive from SonaQube - Optional.ofNullable doesn't return null...
    @SuppressWarnings({"java:S2259"})
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
        XmlElementsPrism.getOptionalOn(analysedComponent.element().getAccessor())
            .ifPresent(outerPrism -> {
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
            });
        return Map.copyOf(extracted);
    }

    private boolean concreteImplementingType (final ProcessingTarget processingTarget) {
        return (processingTarget instanceof AnalysedRecord) || 
            (processingTarget instanceof LibraryLoadedTarget);
    }

    private Optional<AnalysedInterface> extractSupported(final AnalysedComponent analysedComponent) {
        return analysedComponent.targetAnalysedType()
            .filter(x -> (!analysedComponent.requiresUnwrapping()))
            .filter(x -> XmlElementPrism.isPresent(analysedComponent.element().getAccessor()))
            .filter(AnalysedInterface.class::isInstance)
            .map(AnalysedInterface.class::cast);
    }

    private record NameAndNamespace(Optional<String> name, Optional<String> namespace) {}
}
