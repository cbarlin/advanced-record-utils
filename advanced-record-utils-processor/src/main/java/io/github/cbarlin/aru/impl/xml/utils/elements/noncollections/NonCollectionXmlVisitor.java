package io.github.cbarlin.aru.impl.xml.utils.elements.noncollections;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedOptionalComponent;
import io.github.cbarlin.aru.impl.types.TypeAliasComponent;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.micronaut.sourcegen.javapoet.MethodSpec;

import java.util.Optional;

public abstract class NonCollectionXmlVisitor extends XmlVisitor {

    private final Optional<AnalysedComponent> analysedOptionalComponent;
    protected final Optional<TypeAliasComponent> typeAliasComponent;

    protected NonCollectionXmlVisitor(
            final ClaimableOperation claimableOperation,
            final XmlRecordHolder xmlHolder,
            final Optional<AnalysedOptionalComponent> analysedOptionalComponent,
            final Optional<TypeAliasComponent> typeAliasComponent
    ) {
        super(claimableOperation, xmlHolder);
        this.analysedOptionalComponent = analysedOptionalComponent.map(AnalysedComponent.class::cast);
        this.typeAliasComponent = typeAliasComponent;
    }

    abstract protected boolean writeElementMethod(final AnalysedComponent analysedComponent);

    @Override
    protected final boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        return this.writeElementMethod(analysedOptionalComponent.or(() -> typeAliasComponent).orElse(analysedComponent));
    }

    public static void writeStandardStartElement(final MethodSpec.Builder methodBuilder, final String elementName) {
        methodBuilder.beginControlFlow("if (currentDefaultNamespace != null)")
                .addStatement("output.writeStartElement(currentDefaultNamespace, $S)", elementName)
                .nextControlFlow("else")
                .addStatement("output.writeStartElement($S)", elementName)
                .endControlFlow();
    }
}
