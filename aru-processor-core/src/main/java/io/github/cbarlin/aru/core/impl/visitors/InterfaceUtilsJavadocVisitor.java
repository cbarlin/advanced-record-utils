package io.github.cbarlin.aru.core.impl.visitors;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.artifacts.UtilsClass;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.OperationType;
import io.github.cbarlin.aru.core.visitors.InterfaceVisitor;
import io.github.cbarlin.aru.core.wiring.CorePerInterfaceScope;

@Component
@CorePerInterfaceScope
public final class InterfaceUtilsJavadocVisitor extends InterfaceVisitor {

    private final UtilsClass utilsClass;

    protected InterfaceUtilsJavadocVisitor(final AnalysedInterface analysedInterface, final UtilsClass utilsClass) {
        super(new ClaimableOperation("InterfaceUtilsJavadoc", OperationType.CLASS), analysedInterface);
        this.utilsClass = utilsClass;
    }

    @Override
    protected boolean visitInterfaceImpl() {
        utilsClass.builder()
            .addJavadoc("The Utils class for an interface. Serves mostly to point to concrete implementations");
        return true;
    }

    @Override
    public int specificity() {
        return -1;
    }

}
