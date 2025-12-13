package io.github.cbarlin.aru.impl.builder;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants;
import io.github.cbarlin.aru.impl.types.ExtensionMethod;
import io.github.cbarlin.aru.impl.types.RecordWithExtensions;
import io.github.cbarlin.aru.impl.wiring.BuilderPerRecordScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;
import io.micronaut.sourcegen.javapoet.TypeSpec;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.Optional;
import java.util.function.Predicate;

@Component
@BuilderPerRecordScope
@RequiresBean({RecordWithExtensions.class})
public final class AddExtensionMethods extends RecordVisitor {

    private final RecordWithExtensions recordWithExtensions;

    public AddExtensionMethods(final AnalysedRecord analysedRecord, final RecordWithExtensions recordWithExtensions) {
        super(Constants.Claims.BUILDER_ADD_EXTENSION_METHOD, analysedRecord);
        this.recordWithExtensions = recordWithExtensions;
    }

    @Override
    public int specificity() {
        return 1;
    }

    @Override
    protected boolean visitStartOfClassImpl() {
        // First let's add all the interfaces
        addInterfaces();
        // Now we add the methods! They've been pre-validated, and they'll be ordered just before being written out
        for (final ExtensionMethod extensionMethod : recordWithExtensions.extensionMethods()) {
            writeExtensionMethod(extensionMethod);
        }
        return true;
    }

    private void writeExtensionMethod(final ExtensionMethod extensionMethod) {
        final ExecutableElement method = extensionMethod.method();
        final TypeName location = TypeName.get(method.getEnclosingElement().asType());
        final TypeName methodReturn = TypeName.get(method.getReturnType());
        final VariableElement passedThru = method.getParameters().get(1);
        final ParameterSpec param = ParameterSpec.get(passedThru);
        final String name = method.getSimpleName().toString();
        final MethodSpec.Builder builder = analysedRecord.builderArtifact()
            .createMethod(name, this.claimableOperation)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(param)
            .addJavadoc("Calls {@link $T#$L($T, $T)}", location, name, analysedRecord.builderArtifact().className(), param.type);
        if (methodReturn.equals(TypeName.VOID)) {
            builder.returns(analysedRecord.builderArtifact().className())
                .addStatement("$T.$L(this, $L)", location, name, param.name)
                .addStatement("return this");
        } else {
            builder.returns(methodReturn)
                .addStatement("return $T.$L(this, $L)", location, name, param.name);
        }
        AnnotationSupplier.addGeneratedAnnotation(builder, this);
        if (extensionMethod.fromInterface().isPresent()) {
            builder.addAnnotation(Override.class);
        }
    }

    private void addInterfaces() {
        final TypeSpec.Builder builderTs = analysedRecord.builderArtifact().builder();
        // We don't need to worry about order as they will be ordered just before being written out
        recordWithExtensions.extensionMethods()
            .stream()
            .map(ExtensionMethod::fromInterface)
            .flatMap(Optional::stream)
            .distinct()
            .filter(Predicate.not(CommonsConstants.Names.ARU_DEFAULT::equals))
            .forEach(builderTs::addSuperinterface);
    }
}
