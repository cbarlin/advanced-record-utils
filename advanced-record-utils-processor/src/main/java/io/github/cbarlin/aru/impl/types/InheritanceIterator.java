package io.github.cbarlin.aru.impl.types;

import javax.lang.model.element.TypeElement;
import java.util.Iterator;

/**
 * An iterator over all possible parents of a given {@link TypeElement}
 * <p>
 * Note: this <em>will</em> probably repeat elements, depending on the divergent branches
 */
public final class InheritanceIterator implements Iterator<TypeElement> {

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public TypeElement next() {
        return null;
    }

    private final static class InterfaceIterator implements Iterator<TypeElement> {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public TypeElement next() {
            return null;
        }
    }
}
