package io.github.cbarlin.aru.impl.types.maps.eclipse;

import io.github.cbarlin.aru.impl.types.maps.MapHandler;
import io.micronaut.sourcegen.javapoet.TypeName;

/**
 * Marker interface for handlers for Eclipse collection maps
 */
public interface EclipseMapHandler extends MapHandler {
    static String strName(final TypeName typeName) {
        if (TypeName.BOOLEAN.equals(typeName)) {
            return "Boolean";
        } else if (TypeName.BYTE.equals(typeName)) {
            return "Byte";
        } else if (TypeName.SHORT.equals(typeName)) {
            return "Short";
        } else if (TypeName.INT.equals(typeName)) {
            return "Int";
        } else if (TypeName.LONG.equals(typeName)) {
            return "Long";
        } else if (TypeName.CHAR.equals(typeName)) {
            return "Char";
        } else if (TypeName.FLOAT.equals(typeName)) {
            return "Float";
        } else if (TypeName.DOUBLE.equals(typeName)) {
            return "Double";
        } else {
            return "Object";
        }
    }
}
