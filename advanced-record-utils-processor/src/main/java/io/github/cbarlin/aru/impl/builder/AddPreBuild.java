package io.github.cbarlin.aru.impl.builder;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;
import io.github.cbarlin.aru.core.CommonsConstants.Names;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.core.wiring.CorePerRecordScope;
import io.github.cbarlin.aru.impl.types.AnalysedOptionalPrimitiveComponent;
import io.github.cbarlin.aru.impl.types.RecordWithBeforeBuild;
import io.github.cbarlin.aru.impl.wiring.BuilderPerRecordScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;

@Component
@BuilderPerRecordScope
@RequiresBean({RecordWithBeforeBuild.class})
public final class AddPreBuild extends RecordVisitor {
    private static final String CONSTRUCT = "return new $T(\n%s\n)";
    private static final String CALL_GETTER = "this.$L()";
    private final ExecutableElement beforeCall;

    public AddPreBuild(final AnalysedRecord analysedRecord, final RecordWithBeforeBuild recordWithBeforeBuild) {
        super(Claims.BUILDER_BUILD, analysedRecord);
        this.beforeCall = recordWithBeforeBuild.beforeBuild();
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
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Names.NON_NULL);
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        writePreBuildCall(methodBuilder);
        writeStandardBuild(methodBuilder);
    }

    private void writePreBuildCall(final MethodSpec.Builder methodBuilder) {
        logTrace(methodBuilder, "Calling pre-build method");
        final TypeName location = TypeName.get(beforeCall.getEnclosingElement().asType());
        methodBuilder.addStatement("$T.$L(this)", location, beforeCall.getSimpleName().toString());
    }

    private void writeStandardBuild(final MethodSpec.Builder methodBuilder) {
        logTrace(methodBuilder, "Creating new instance");
        final List<String> constructorArgs = new ArrayList<>();
        final List<Object> formatArgs = new ArrayList<>();
        formatArgs.add(analysedRecord.className());
        for ( final VariableElement param : analysedRecord.intendedConstructor().getParameters()) {
            constructorArgs.add(CALL_GETTER);
            formatArgs.add(param.getSimpleName().toString());
        }
        final String command = CONSTRUCT.formatted(StringUtils.join(constructorArgs, ",\n\t"));
        final Object[] args = formatArgs.toArray();
        methodBuilder.addStatement(command, args);
    }
}
