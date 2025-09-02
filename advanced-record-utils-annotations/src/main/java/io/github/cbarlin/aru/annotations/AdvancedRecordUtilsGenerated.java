package io.github.cbarlin.aru.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Annotation used to aid in loading references from e.g. a library, as well as to convey that the code was generated.
 * <p>
 * Intended for internal use only
 */
@Target({ TYPE })
@Retention(RetentionPolicy.CLASS)
public @interface AdvancedRecordUtilsGenerated {

    public static final String XML_DEFAULT_STRING = "##default";

    Class<?> generatedFor();

    Version version();

    AdvancedRecordUtils settings();

    InternalUtil[] internalUtils();

    Class<?>[] usedTypeConverters() default {};

    Class<? extends GeneratedUtil>[] references();

    public @interface InternalUtil {
        String type();

        Class<?> implementation();
    }

    public @interface Version {
        public static final int MAJOR_VERSION = 0;
        public static final int MINOR_VERSION = 6;
        public static final int PATCH_VERSION = 4;

        int major() default MAJOR_VERSION;

        int minor() default MINOR_VERSION;

        int patch() default PATCH_VERSION;
    }
}