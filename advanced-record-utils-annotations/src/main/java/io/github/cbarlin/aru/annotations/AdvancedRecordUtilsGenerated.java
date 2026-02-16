package io.github.cbarlin.aru.annotations;

import java.lang.annotation.Annotation;
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

    /**
     * The class this was generated for
     *
     * @return The target class
     */
    Class<?> generatedFor();

    /**
     * The version of the utils processor that generated the class.
     * <p>
     * Useful for debugging purposes, as well as possibly used by the processor for backwards compatibility when loading libraries
     * @return The version information
     */
    Version version();

    /**
     * The final set of settings that the user specified
     *
     * @return The settings, but only those that the user set
     */
    AdvancedRecordUtils settings();

    /**
     * The details of the items that are internal to the utils class
     * <p>
     * Mostly used as a "table of contents" for the class
     *
     * @return All the internal classes of the generated class
     */
    InternalUtil[] internalUtils() default {};

    /**
     * Classes containing type converters used or known by this utils class
     * <p>
     * Note that we document the classes, not the actual methods because there's no way to reference a specific method...
     *
     * @return The classes containing converters
     * @since 0.6.1
     */
    Class<?>[] usedTypeConverters() default {};

    /**
     * Utils classes that are referenced from/to this one
     *
     * @return The utils classes that belong in the same "tree" of references
     */
    Class<? extends GeneratedUtil>[] references() default {};

    /**
     * The sources of the {@link #settings()} attribute, including their settings
     *
     * @return The sources of the settings
     * @since 0.6.6
     */
    SettingsSource[] settingSources() default {};

    /**
     * Information about root elements known at processing time
     *
     * @return The elements that are the roots of the trees
     * @since 0.6.6
     */
    RootElementInformation[] rootElements() default {};

    /**
     * Classes from the processing round about known meta-annotations
     * <p>
     * It should be populated onto each known root utils class.
     * <p>
     * Non-root elements shouldn't have this populated
     *
     * @return The classes that are meta-annotations
     * @since 0.6.6
     */
    Class<? extends Annotation>[] knownMetaAnnotations() default {};

    /**
     * The full suite of settings used to generate the class
     *
     * @return The full suite of settings, including defaults
     * @since 0.6.6
     */
    AdvancedRecordUtils fullyComputedSettings() default @AdvancedRecordUtils;

    /**
     * Information about a root element in a processing tree.
     * <p>
     * For non-root utils classes, this should only be the root elements that reference this one.
     * <p>
     * For root utils classes, this should contain all other known root elements
     *
     * @since 0.6.6
     */
    @Retention(RetentionPolicy.CLASS)
    @Target({})
    public @interface RootElementInformation {

        /**
         * The type that was considered a root of the processing tree.
         * <p>
         * The {@link io.github.cbarlin.aru.annotations.AdvancedRecordUtils.DEFAULT} class is a sentinel value to denote that the
         *  root element was not on a type
         * @return The type that was considered the root
         * @since 0.6.6
         */
        Class<?> rootType() default AdvancedRecordUtils.DEFAULT.class;

        /**
         * The package that was considered the root of the processing tree
         * <p>
         * A blank string is a sentinel value to denote that the root was not a package
         *
         * @return The package that was considered the root
         * @since 0.6.6
         */
        String rootPackage() default "";

        /**
         * The resulting Utils class that was created for the element
         * <p>
         * The {@link GeneratedUtil} class is a sentinel value to denote that there wasn't a Utils class generated (e.g. a package)
         *
         * @return The resulting utils class
         * @since 0.6.6
         */
        Class<? extends GeneratedUtil> utilsClass() default GeneratedUtil.class;

        /**
         * Did this root element reference this current item?
         * <p>
         * Packages are always considered to reference the classes within them.
         *
         * @return If this root element references this current item?
         * @since 0.6.6
         */
        boolean referencesCurrentItem() default true;
    }

    /**
     * Describes a source of a setting that influenced the chosen settings for a Utils class
     *
     * @since 0.6.6
     */
    @Retention(RetentionPolicy.CLASS)
    @Target({})
    public @interface SettingsSource {

        /**
         * The settings that were specified by the element
         *
         * @return The settings that were specified by the element
         * @since 0.6.6
         */
        AdvancedRecordUtils settings() default @AdvancedRecordUtils();

        /**
         * The type that had an annotation on it.
         * <p>
         * The {@link io.github.cbarlin.aru.annotations.AdvancedRecordUtils.DEFAULT} class is a sentinel value to denote that the
         *  annotation was not on a type.
         *
         * @return The type that was considered the root
         * @since 0.6.6
         */
        Class<?> annotatedType() default AdvancedRecordUtils.DEFAULT.class;

        /**
         * The package that was annotated
         * <p>
         * A blank string is a sentinel value to denote that these settings were not on a package.
         *
         * @return The package that was considered the root
         * @since 0.6.6
         */
        String annotatedPackage() default "";

        /**
         * The module that was annotated
         * <p>
         * A blank string is a sentinel value to denote that these settings were not on a module.
         *
         * @return The package that was considered the root
         * @since 0.6.6
         */
        String annotatedModule() default "";

        /**
         * The distance of the settings from the resulting element.
         * <p>
         * Aside from some exceptions, a setting on a further away element will be overridden by the setting on a closer element
         *
         * @return The distance from the current element
         * @since 0.6.6
         */
        int distance() default 1;
    }

    /**
     * A description of an item internal to the utils class
     */
    @Retention(RetentionPolicy.CLASS)
    @Target({})
    public @interface InternalUtil {
        /**
         * The type of item generated
         *
         * @return The type of item
         * @since 0.6.6
         */
        String type();

        /**
         * The implementing sub-class
         *
         * @return The implementation
         * @since 0.6.6
         */
        Class<?> implementation();
    }

    /**
     * Information about the version of the processor that was used
     */
    @Retention(RetentionPolicy.CLASS)
    @Target({})
    public @interface Version {
        /**
         * The major version of the library
         */
        public static final int MAJOR_VERSION = 0;
        /**
         * The minor version of the library
         */
        public static final int MINOR_VERSION = 8;
        /**
         * The patch version of the library
         */
        public static final int PATCH_VERSION = 0;

        /**
         * The major version of the generator used
         *
         * @return The major version of the generator
         */
        int major() default MAJOR_VERSION;

        /**
         * The minor version of the generator used
         *
         * @return The minor version of the generator
         */
        int minor() default MINOR_VERSION;

        /**
         * The patch version of the generator used
         *
         * @return The patch version of the generator
         */
        int patch() default PATCH_VERSION;
    }
}