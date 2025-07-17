package io.github.cbarlin.aru.core;

import java.io.IOException;
import java.io.Writer;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtilsGenerated;
import io.github.cbarlin.aru.core.visitors.InterfaceVisitor;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;

import io.avaje.prism.GenerateAPContext;
import io.avaje.prism.GeneratePrism;
import io.avaje.prism.GenerateUtils;

@GenerateUtils
@GenerateAPContext
@GeneratePrism(value = AdvancedRecordUtils.class, publicAccess = true)
@GeneratePrism(value = AdvancedRecordUtilsGenerated.class, publicAccess = true)
public final class AdvRecUtilsProcessor extends AbstractProcessor {

    private static final String META_ANNOTATION_RESOURCE_PATH = "META-INF/cbarlin/metaannotations/io.github.cbarlin.aru.annotations.AdvancedRecordUtils";
    private static final Supplier<List<RecordVisitor>> RECORD_VISITORS = () -> {
        final List<RecordVisitor> sortable = new ArrayList<>();
        ServiceLoader.load(RecordVisitor.class, RecordVisitor.class.getClassLoader())
            .iterator()
            .forEachRemaining(sortable::add);
        // Reverse the order, we want the most specific options first
        if (sortable.isEmpty()) {
            APContext.messager().printError(
                "There are no elements loaded by the service loader"
            );
        }
        Collections.sort(sortable);
        return List.copyOf(sortable);
    };

    private static final Supplier<List<InterfaceVisitor>> INTERFACE_VISITORS = () -> {
        final List<InterfaceVisitor> sortable = new ArrayList<>();
        ServiceLoader.load(InterfaceVisitor.class, InterfaceVisitor.class.getClassLoader())
            .iterator()
            .forEachRemaining(sortable::add);
        // Interfaces we don't mind if there are no found operations, so no error here
        Collections.sort(sortable);
        return List.copyOf(sortable);
    };

    private final Set<String> supportedAnnotations = new HashSet<>();
    private UtilsProcessingContext context;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        // First, find any meta-annotations
        findMetaAnnotations(roundEnv);
        // OK, now loop through all those and analyse them!
        findGenerationTargets(roundEnv);
        // Build all the utils classes
        context.processElements(RECORD_VISITORS, INTERFACE_VISITORS);

        if(roundEnv.processingOver()) {
            APContext.clear();
        }
        return true;
    }

    private void findGenerationTargets(final RoundEnvironment roundEnv) {
        for(final String annotation : Set.copyOf(supportedAnnotations)) {
            final TypeElement annoType = processingEnv.getElementUtils().getTypeElement(annotation);
            for(final Element annotatedElement : roundEnv.getElementsAnnotatedWith(annoType)) {
                context.analyseRootElement(annotatedElement);
            }
        }
    }

    private void findMetaAnnotations(final RoundEnvironment roundEnv) {
        for(final String annotation : Set.copyOf(supportedAnnotations)) {
            final TypeElement annoType = processingEnv.getElementUtils().getTypeElement(annotation);
            for(final Element annotatedElement : roundEnv.getElementsAnnotatedWith(annoType)) {
                if (annotatedElement instanceof TypeElement typeAnnoElement && ElementKind.ANNOTATION_TYPE.equals(typeAnnoElement.getKind())) {
                    // This is an annotation!
                    final String name = typeAnnoElement.getQualifiedName().toString();
                    try {
                        try(final Writer w = APContext.filer()
                            .createResource(
                                StandardLocation.CLASS_OUTPUT, 
                                "", 
                                META_ANNOTATION_RESOURCE_PATH + "/" + name, 
                                typeAnnoElement
                            )
                            .openWriter();) {
                                w.append(name);
                            }
                    } catch (Exception e) {
                        APContext.messager().printError("Error writing meta annotation: " + e.getMessage());
                    }
                    supportedAnnotations.add(name);
                }
            }
        }
    }

    @Override
    public synchronized void init(final ProcessingEnvironment env) {
        super.init(env);
        APContext.init(env);
        context = new UtilsProcessingContext(env);
        try {
            loadSupportedAnnotations();
        } catch (final Exception ex) {
            env.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to load meta-annotations from classpath: " + ex.getMessage());
        }
    }

    private void loadSupportedAnnotations() throws IOException {
        supportedAnnotations.add(AdvancedRecordUtilsPrism.PRISM_TYPE);
        supportedAnnotations.add(AdvancedRecordUtilsGeneratedPrism.PRISM_TYPE);

        final Messager messager = APContext.messager();

        ClassLoader classLoader = RecordVisitor.class.getClassLoader();
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        if (classLoader == null) {
            messager.printMessage(Diagnostic.Kind.WARNING, "Could not obtain ClassLoader to search for resources.");
            return; 
        }

        final Enumeration<URL> resourceUrls = classLoader.getResources(META_ANNOTATION_RESOURCE_PATH);
        while(resourceUrls.hasMoreElements()) {
            final URL dirUrl = resourceUrls.nextElement();
            try {
                if ("file".equals(dirUrl.getProtocol())) {
                    loadSupportedAnnotationsFromFile(dirUrl);
                } else if ("jar".equals(dirUrl.getProtocol())) {
                    loadSupportedAnnotationsFromJar(dirUrl);
                } else {
                     messager.printMessage(Diagnostic.Kind.WARNING, "Unsupported protocol for resource URL: " + dirUrl);
                }
            } catch (final URISyntaxException e) {
                messager.printMessage(Diagnostic.Kind.WARNING, "Invalid URI syntax for resource URL: " + dirUrl + ", Error: " + e.getMessage());
            } catch (final IOException e) {
                messager.printMessage(Diagnostic.Kind.WARNING, "Could not process resource URL: " + dirUrl + ", Error: " + e.getMessage());
            }
        }
    }

    private void loadSupportedAnnotationsFromJar(final URL dirUrl) throws IOException {
        final JarURLConnection jarURLConnection = (JarURLConnection) dirUrl.openConnection();
        jarURLConnection.setUseCaches(false); 
        try (JarFile jarFile = jarURLConnection.getJarFile()) {
            final String entryPrefix = jarURLConnection.getEntryName() + "/";

            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                final String entryName = entry.getName();
                if (!entry.isDirectory() && entryName.startsWith(entryPrefix)) {
                    final String fileName = entryName.substring(entryPrefix.length());
                    if (!fileName.isEmpty() && !fileName.contains("/")) {
                        supportedAnnotations.add(fileName);
                    }
                }
            }
        }
    }

    private void loadSupportedAnnotationsFromFile(final URL dirUrl) throws URISyntaxException, IOException {
        final Path dirPath = Paths.get(dirUrl.toURI());
        if (Files.isDirectory(dirPath)) {
            try (Stream<Path> stream = Files.list(dirPath)) {
                stream.filter(Files::isRegularFile)
                      .map(path -> path.getFileName().toString())
                      .forEach(supportedAnnotations::add);
            }
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.copyOf(supportedAnnotations);
    }
}
