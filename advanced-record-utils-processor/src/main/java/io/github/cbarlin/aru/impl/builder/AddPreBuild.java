package io.github.cbarlin.aru.impl.builder;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;
import io.github.cbarlin.aru.core.impl.visitors.builder.AddPlainBuild;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.types.RecordWithBeforeBuild;
import io.github.cbarlin.aru.impl.wiring.BuilderPerRecordScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

@Component
@BuilderPerRecordScope
@RequiresBean({RecordWithBeforeBuild.class})
public final class AddPreBuild extends RecordVisitor {
    private final ExecutableElement beforeCall;
    private final AddPlainBuild addPlainBuild;

    public AddPreBuild(final AnalysedRecord analysedRecord, final RecordWithBeforeBuild recordWithBeforeBuild) {
        super(Claims.BUILDER_BUILD, analysedRecord);
        this.beforeCall = recordWithBeforeBuild.beforeBuild();
        this.addPlainBuild = new AddPlainBuild(analysedRecord);
    }

    @Override
    public int specificity() {
        return 1;
    }

    @Override
    protected void visitEndOfClassImpl() {
        final String methodName = analysedRecord.settings().prism().builderOptions().buildMethodName();
        final var methodBuilder = analysedRecord.builderArtifact()
            .createMethod(methodName, claimableOperation)
            .returns(analysedRecord.intendedType())
            .addJavadoc("Creates a new instance of {@link $T} from the fields set on this builder\n<p>\nWill call the requested method before continuing with the build", analysedRecord.intendedType())
            .addModifiers(Modifier.PUBLIC);
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        writePreBuildCall(methodBuilder);
        addPlainBuild.addConstruction(methodBuilder);
    }

    private void writePreBuildCall(final MethodSpec.Builder methodBuilder) {
        logTrace(methodBuilder, "Calling pre-build method");
        final TypeName location = TypeName.get(beforeCall.getEnclosingElement().asType());
        methodBuilder.addStatement("$T.$L(this)", location, beforeCall.getSimpleName().toString());
    }
}
