package io.github.cbarlin.aru.impl.builder;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.visitors.InterfaceVisitor;
import io.github.cbarlin.aru.impl.wiring.BuilderPerInterfaceScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@Component
@BuilderPerInterfaceScope
public final class IfaceAddBuilderCopy extends InterfaceVisitor {

    public IfaceAddBuilderCopy(final AnalysedInterface analysedInterface) {
        super(Claims.BUILDER_FROM_EXISTING, analysedInterface);
    }

    @Override
    protected boolean visitInterfaceImpl() {
        final String bridge = utilsPrism.builderOptions().multiTypeSetterBridge();
        final String builderName = utilsPrism.builderOptions().copyCreationName();
        for(final ProcessingTarget target : analysedInterface.implementingTypes()) {
            if (target instanceof AnalysedInterface) {
                continue;
            }
            final TypeElement typeElement = target.typeElement();
            final TypeName retType = (target instanceof AnalysedRecord analysedRecord) ? analysedRecord.intendedType() : TypeName.get(typeElement.asType());
            final String name = typeElement.getSimpleName().toString();

            final String builderMethodName = builderName + bridge + name;
            final ClassName otherBuilderName = target.builderArtifact().className();
            final String otherBuildCopyName = target.prism().builderOptions().copyCreationName();

            final MethodSpec.Builder methodBuilder = analysedInterface.utilsClass().createMethod(builderMethodName, claimableOperation, typeElement)
                .addModifiers(Modifier.STATIC, Modifier.FINAL)
                .addJavadoc("Obtain the builder for {@link $T}", retType)
                .returns(otherBuilderName)
                .addParameter(
                    ParameterSpec.builder(retType, "existing", Modifier.FINAL)
                        .addJavadoc("The instance to copy")
                        .build()
                )
                .addStatement("return $T.$L(existing)", otherBuilderName, otherBuildCopyName);

            analysedInterface.addCrossReference(target);
            AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        }
        return true;
    }

    @Override
    public int specificity() {
        return 0;
    }

}
