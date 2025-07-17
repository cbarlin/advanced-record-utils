package io.github.cbarlin.aru.impl.builder;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.visitors.InterfaceVisitor;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public final class IfaceAddBuilderEmpty extends InterfaceVisitor {

    public IfaceAddBuilderEmpty() {
        super(Claims.CORE_BUILDER_FROM_NOTHING);
    }

    @Override
    protected boolean visitInterfaceImpl(final AnalysedInterface analysedInterface) {
        final String bridge = analysedInterface.prism().builderOptions().multiTypeSetterBridge();
        final String builderName = analysedInterface.prism().builderOptions().emptyCreationName();
        for(final ProcessingTarget target : analysedInterface.implementingTypes()) {
            if (target instanceof AnalysedInterface) {
                continue;
            }
            final TypeElement typeElement = target.typeElement();
            final TypeName retType = (target instanceof AnalysedRecord analysedRecord) ? analysedRecord.intendedType() : TypeName.get(typeElement.asType());
            final String name = typeElement.getSimpleName().toString();

            final String builderMethodName = builderName + bridge + name;
            final ClassName otherBuilderName = target.builderArtifact().className();
            final String otherBuildEmptyName = target.prism().builderOptions().emptyCreationName();

            final MethodSpec.Builder methodBuilder = analysedInterface.utilsClass().createMethod(builderMethodName, claimableOperation, typeElement)
                .addModifiers(Modifier.STATIC, Modifier.FINAL)
                .addJavadoc("Obtain the builder for {@link $T}", retType)
                .returns(otherBuilderName)
                .addStatement("return $T.$L()", otherBuilderName, otherBuildEmptyName);

            analysedInterface.addCrossReference(target);
            AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        }
        return true;
    }

    @Override
    public int specificity() {
        return 0;
    }

    @Override
    public boolean isApplicable(final AnalysedInterface target) {
        return true;
    }

}
