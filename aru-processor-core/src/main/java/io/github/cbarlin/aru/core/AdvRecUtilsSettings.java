package io.github.cbarlin.aru.core;

import io.github.cbarlin.aru.core.mirrorhandlers.MergedMirror;
import io.github.cbarlin.aru.core.mirrorhandlers.SourceTrackingAnnotationMirror;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import org.jspecify.annotations.NullMarked;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Holder for the settings being used in the current processing context of an element.
 */
@NullMarked
public final class AdvRecUtilsSettings {
    private static final String ANNOTATION_TYPE = AdvancedRecordUtilsPrism.PRISM_TYPE;
    private final AdvancedRecordUtilsPrism prism;
    private final Element originalElement;

    public AdvRecUtilsSettings(final AnnotationMirror originalMirror, final Element element) {
        if ((!(element instanceof TypeElement)) && (!(element instanceof PackageElement))) {
            throw new IllegalArgumentException("Element must be either Type or Package");
        }
        this.prism = Objects.requireNonNull(AdvancedRecordUtilsPrism.getInstance(originalMirror));
        this.originalElement = element;
    }

    public AdvRecUtilsSettings(final AnnotationMirror originalMirror) {
        this(originalMirror, null);
    }

    public String builderClassName() {
        return prism.builderOptions().builderName();
    }

    public AdvancedRecordUtilsPrism prism() {
        return this.prism;
    }

    public AnnotationMirror mirror() {
        return this.prism.mirror;
    }

    public Element originalElement() {
        return this.originalElement;
    }

    public String packageName() {
        if (originalElement instanceof final TypeElement te) {
            return ClassName.get(te).packageName();
        } else if (originalElement instanceof final PackageElement pe) {
            return pe.getQualifiedName().toString();
        }
        throw new IllegalStateException("Internal element is not a TypeElement or PackageElement");
    }

    private static final Map<PackageElement, Optional<AnnotationMirror>> PACKAGE_MIRRORS = new HashMap<>();

    private static Optional<AnnotationMirror> obtainMirrorOnPackageElement(final PackageElement pkgEl, final ProcessingEnvironment env) {
        return PACKAGE_MIRRORS.computeIfAbsent(pkgEl, ignored -> obtainMirrorOnElement(pkgEl, env));
    } 

    private static Optional<AnnotationMirror> obtainMirrorOnElement(final Element element, final ProcessingEnvironment env) {
        AnnotationMirror foundMirror = null;
        final Set<String> seen = new HashSet<>();
        final LinkedList<AnnotationMirror> mirrors = new LinkedList<>();
        element.getAnnotationMirrors()
            .stream()
            .filter(m -> seen.add(m.getAnnotationType().toString()))
            .map(mirror -> new SourceTrackingAnnotationMirror(mirror, element))
            .forEach(mirrors::add);
        env.getElementUtils().getAllAnnotationMirrors(element)
            .stream()
            .filter(m -> seen.add(m.getAnnotationType().toString()))
            .map(mirror -> new SourceTrackingAnnotationMirror(mirror, element))
            .forEach(mirrors::add);
        while (!mirrors.isEmpty()) {
            final AnnotationMirror inspect = mirrors.removeFirst();
            if (ANNOTATION_TYPE.equals(inspect.getAnnotationType().toString())) {
                // Yay!
                if(Objects.isNull(foundMirror)) {
                    foundMirror = inspect;
                } else {
                    foundMirror = new MergedMirror(foundMirror, inspect);
                }
            } else {
                final Element nxtLevel = env.getTypeUtils().asElement(inspect.getAnnotationType());
                env.getElementUtils().getAllAnnotationMirrors(nxtLevel)
                   .stream()
                   .filter(m -> seen.add(m.getAnnotationType().toString()))
                   .map(mirror -> new SourceTrackingAnnotationMirror(mirror, nxtLevel))
                   .forEach(mirrors::add);
            }
        }
        final var elMirror = Optional.ofNullable(foundMirror);
        // This should also pull in settings from the package layer
        final var pkgMir = Optional.of(element)
            .map(Element::getKind)
            .filter(Predicate.not(ElementKind.PACKAGE::equals))
            .map(nPkg -> env.getElementUtils().getPackageOf(element))
            .flatMap(pEl -> obtainMirrorOnPackageElement(pEl, env));
        
        if (pkgMir.isEmpty()) {
            return elMirror;
        } else if (elMirror.isPresent()) {
            return elMirror.map(pref -> new MergedMirror(pref, pkgMir.get()));
        } else  {
            return pkgMir;
        }
    }

    public static Optional<AdvRecUtilsSettings> wrapOptional(final Element element, final ProcessingEnvironment env) {
        return obtainMirrorOnElement(element, env)
            .map(mirr -> new AdvRecUtilsSettings(mirr, element));
    }

    public static boolean isAnnotatated(final Element element, final ProcessingEnvironment env) {
        return obtainMirrorOnElement(element, env).isPresent();
    }

    public static AdvRecUtilsSettings wrap(final Element element, final ProcessingEnvironment env) {
        final var opt = wrapOptional(element, env);
        if (opt.isEmpty()) {
            env.getMessager()
                .printMessage(Diagnostic.Kind.ERROR, "Was asked to wrap an element, but cannot find the annotation", element);
        }
        return opt.orElseThrow();
    }

    public static AdvRecUtilsSettings merge(final AdvRecUtilsSettings preferred, final AdvRecUtilsSettings secondary) {
        final AnnotationMirror merged = new MergedMirror(preferred.mirror(), secondary.mirror());
        return new AdvRecUtilsSettings(merged, preferred.originalElement());
    }
}
