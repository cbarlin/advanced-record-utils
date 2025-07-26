package io.github.cbarlin.aru.core.meta;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.core.wiring.AruGlobal;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;

@Factory
@AruGlobal
public final class SupportedAnnotationsFactory {

    private static final String META_ANNOTATION_RESOURCE_PATH = "META-INF/cbarlin/metaannotations/io.github.cbarlin.aru.annotations.AdvancedRecordUtils";

    @Bean
    @AruGlobal
    SupportedAnnotations initialAnnotations() throws IOException {
        final HashSet<String> supportedAnnotations = new HashSet<>();
        supportedAnnotations.add(AdvancedRecordUtilsPrism.PRISM_TYPE);

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
        }

        final Enumeration<URL> resourceUrls = classLoader.getResources(META_ANNOTATION_RESOURCE_PATH);
        while(resourceUrls.hasMoreElements()) {
            final URL dirUrl = resourceUrls.nextElement();
            try {
                if ("file".equals(dirUrl.getProtocol())) {
                    loadSupportedAnnotationsFromFile(dirUrl, supportedAnnotations);
                } else if ("jar".equals(dirUrl.getProtocol())) {
                    loadSupportedAnnotationsFromJar(dirUrl, supportedAnnotations);
                } else {
                     messager.printMessage(Diagnostic.Kind.WARNING, "Unsupported protocol for resource URL: " + dirUrl);
                }
            } catch (final URISyntaxException e) {
                messager.printMessage(Diagnostic.Kind.WARNING, "Invalid URI syntax for resource URL: " + dirUrl + ", Error: " + e.getMessage());
            } catch (final IOException e) {
                messager.printMessage(Diagnostic.Kind.WARNING, "Could not process resource URL: " + dirUrl + ", Error: " + e.getMessage());
            }
        }

        return new SupportedAnnotations(supportedAnnotations);
    }

    private void loadSupportedAnnotationsFromJar(final URL dirUrl, final Set<String> supportedAnnotations) throws IOException {
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

    private void loadSupportedAnnotationsFromFile(final URL dirUrl, final Set<String> supportedAnnotations) throws URISyntaxException, IOException {
        final Path dirPath = Paths.get(dirUrl.toURI());
        if (Files.isDirectory(dirPath)) {
            try (Stream<Path> stream = Files.list(dirPath)) {
                stream.filter(Files::isRegularFile)
                      .map(path -> path.getFileName().toString())
                      .forEach(supportedAnnotations::add);
            }
        }
    }
}
