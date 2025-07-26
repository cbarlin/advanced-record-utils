package io.github.cbarlin.aru.core;

import java.io.Writer;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;

import io.avaje.inject.BeanScope;
import io.avaje.prism.GenerateAPContext;
import io.avaje.prism.GenerateUtils;
import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.analysers.TargetAnalyser;
import io.github.cbarlin.aru.core.meta.SupportedAnnotations;

@ServiceProvider
@GenerateUtils
@GenerateAPContext
public final class AdvRecUtilsProcessor extends AbstractProcessor {

    private static final String META_ANNOTATION_RESOURCE_PATH = "META-INF/cbarlin/metaannotations/io.github.cbarlin.aru.annotations.AdvancedRecordUtils";
    private BeanScope globalBeanScope;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        final SupportedAnnotations supportedAnnotations = globalBeanScope.get(SupportedAnnotations.class);

        // First, find any meta-annotations
        findMetaAnnotations(roundEnv, supportedAnnotations.annotations());

        // OK, now loop through all those and analyse them!
        findAndProcessTargets(roundEnv, supportedAnnotations.annotations());

        if(roundEnv.processingOver()) {
            APContext.clear();
        }
        return true;
    }

    private void findAndProcessTargets(final RoundEnvironment roundEnv, final Set<String> supportedAnnotations) {
        final UtilsProcessingContext context = globalBeanScope.get(UtilsProcessingContext.class);
        final List<TargetAnalyser> analysers = globalBeanScope.listByPriority(TargetAnalyser.class);
        for(final String annotation : Set.copyOf(supportedAnnotations)) {
            final TypeElement annoType = processingEnv.getElementUtils().getTypeElement(annotation);
            for(final Element annotatedElement : roundEnv.getElementsAnnotatedWith(annoType)) {
                context.analyseRootElement(annotatedElement, analysers);
            }
        }
        context.matchInterfaces();
        context.processElements(globalBeanScope);
    }

    private void findMetaAnnotations(final RoundEnvironment roundEnv, final Set<String> supportedAnnotations) {
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

        this.globalBeanScope = BeanScope.builder()
            .profiles("aru-global")
            .bean(ProcessingEnvironment.class, env)
            .build();
    }   

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.copyOf(globalBeanScope.get(SupportedAnnotations.class).annotations());
    }
}
