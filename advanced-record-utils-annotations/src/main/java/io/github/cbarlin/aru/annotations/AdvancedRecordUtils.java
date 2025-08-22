package io.github.cbarlin.aru.annotations;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.MODULE;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;

/**
 * Indicates that the target type should have elements created for it.
 * <p>
 * If applied to a record, it will (optionally) automatically create an interface for it, and use the various settings to inform what it populates into the "Utils" class.
 * <p>
 * So a record named {@code MyRecord} will create {@code MyRecordUtils} (which contains {@code MyRecordUtils.Builder}, {@code MyRecordUtils.With}, etc)
 * <p>
 * If applied to a package, then only {@link #importTargets()} is considered (unless {@link #applyToAllInPackage()} is true)
 * <p>
 * This annotation can also be used as a meta-annotation (target: {@code ANNOTATION_TYPE}) to compose your own presets with opinionated defaults for your codebase.
 */
@Target({PACKAGE, TYPE, ANNOTATION_TYPE})
@Retention(RetentionPolicy.CLASS)
@Inherited
public @interface AdvancedRecordUtils {
    //#region Records in scope

    /**
     * Classes excluded by recursive iteration
     */
    Class<?>[] recursiveExcluded() default {};

    /**
     * Classes that are imported into the current generation (e.g. external dependencies)
     * <p>
     * Useful if you cannot control the source of the objects.
     */
    Class<?>[] importTargets() default {};

    /**
     * When the annotation is applied to a package, should this annotation process all records and interfaces in this package?
     */
    boolean applyToAllInPackage() default false;

    /**
     * Do we attempt to find a pre-made instance of a *Utils class?
     * <p>
     * Note: enabling this may take some time to find possible classes
     */
    boolean attemptToFindExistingUtils() default false;

    /**
     * Informs types that they should instead generate targeting an interface instead of the
     *  record's own type.
     * <p>
     * The record will still be the item being constructed, merged etc.
     */
    Class<?> useInterface() default DEFAULT.class;
    //#endregion

    //#region Generation Scope
    /**
     * Should the creator generate a wither?
     */
    boolean wither() default true;

    /**
     * Should the creator generate a merger?
     */
    boolean merger() default false;

    /**
     * Should the creator generate an XML interface?
     */
    boolean xmlable() default false;

    /**
     * Should the creator generate a diff interface?
     */
    boolean diffable() default false;

    /**
     * Options for generating log lines in output code.
     * <p>
     * Logging is mostly done at debug or at trace level. Logging also includes the following attributes as key/values
     * so you can easily filter logs even if you use a per-utils or per-subclass approach to logging:
     * <p>
     * <ul>
     * <li>advancedRecordUtilsProcessor - a constant, "io.github.cbarlin.aru.core.AdvRecUtilsProcessor"</li>
     * <li>originalType - this is the FQN of the record class that we are generating code for</li>
     * <li>intendedType - this is the type that the builder will build for you. It may be the same as "originalType". See {@link #useInterface()}</li>
     * <li>advancedRecordUtilsVisitor - an internal reference for AdvancedRecordUtils maintainers. It's the FQN of the class
     * that generated the method (and thus, the log line)</li>
     * <li>claimedOperationName - an internal reference. It's the "claim" that was obtained to generate the method</li>
     * <li>claimedOperationType - for the aforementioned claim, was it made at a class or component level</li>
     * </ul>
     */
    LoggingGeneration logGeneration() default LoggingGeneration.NONE;

    /**
     * Should we create an "all" interface that wraps up all generated interfaces into one?
     */
    boolean createAllInterface() default true;

    /**
     * Should an Avaje Jsonb import annotation be created for you
     * <p>
     * Useful if you want to avoid annotating every type in a hierarchy or an entire package.
     * <p>
     * Note: Enabling this requires Avaje JSONB annotations on the compilation classpath (optional dependency).
     */
    boolean addJsonbImportAnnotation() default false;
    //#endregion

    //#region Generated target options
    /**
     * Any settings that should be applied to the types (utils, record, interface) created
     */
    TypeNameOptions typeNameOptions() default @TypeNameOptions();

    /**
     * Any settings that should be applied to the Builder
     * <p>
     * Only relevant if builder is enabled
     */
    BuilderOptions builderOptions() default @BuilderOptions();

    /**
     * Any settings that should be applied to the Wither
     * <p>
     * Only relevant if wither is enabled
     */
    WitherOptions witherOptions() default @WitherOptions();

    /**
     * Any settings that should be applied to the Merger
     * <p>
     * Only relevant if mergeable is enabled
     */
    MergerOptions mergerOptions() default @MergerOptions();

    /**
     * Any settings that should be applied to the XML utils
     * <p>
     * Only relevant if xmlable is enabled
     */
    XmlOptions xmlOptions() default @XmlOptions();

    /**
     * Any settings that should be applied to the Diffing
     * <p>
     * Only relevant if diffable is enabled
     */
    DiffOptions diffOptions() default @DiffOptions();
    //#endregion

    //#region Enums
    /**
     * Determine what type of collection should be built
     */
    public enum BuiltCollectionType {
        /**
         * If a collection is provided via calling a "set" method, then that will be used.
         * <p>
         * If a collection is modified via calling an "add" method, even after "set", then a mutable version (ArrayList, HashSet) will be returned
         * <p>
         * If a collection value was never set, or was set to null, an empty mutable collection is returned
         */
        AUTO,
        /**
         * Created collection will be an Immutable collection provided by Java
         */
        JAVA_IMMUTABLE
    }

    /**
     * Determines how we interact with libraries
     */
    public enum LibraryIntegration {
        /**
         * If the library is detected it is used, otherwise it isn't.
         * <p>
         * Detection <em>should</em> work off of the dependencies in your project
         * <p>
         * If there's a false positive, switch to "NEVER"
         */
        IF_DETECTED,
        /**
         * Never generate library integrations
         */
        NEVER,
        /**
         * Skip detection of libraries and always generate an integration
         */
        ALWAYS
    }

    /**
     * Determine options for log generation within utils classes
     */
    public enum LoggingGeneration {
        /**
         * Do not generate logging output. Comments are generated instead
         */
        NONE,
        /**
         * Generate logging output using SLF4J where the logs are for the {@link GeneratedUtil} interface.
         * <p>
         * If generating log lines, do consider this one as it makes it easy to silence just AdvancedRecordUtils log lines across the entire
         * app while leaving debug or trace turned on
         * <p>
         * Note: Selecting this option generates SLF4J-based logging calls; ensure SLF4J is available on the classpath (optional dependency).
         */
        SLF4J_GENERATED_UTIL_INTERFACE,
        /**
         * Generate logging output using SLF4J where the logs are for the wrapping "Utils" class
         * <p>
         * Note: Selecting this option generates SLF4J-based logging calls; ensure SLF4J is available on the classpath (optional dependency).
         */
        SLF4J_UTILS_CLASS,
        /**
         * Generate logging output using SLF4J where the logs are for each subclass
         * <p>
         * Note: Selecting this option generates SLF4J-based logging calls; ensure SLF4J is available on the classpath (optional dependency).
         */
        SLF4J_PER_SUBCLASS
    }

    /**
     * The type of validation API that can be provided to the builder
     */
    public enum ValidationApi {
        /**
         * No validation API should be used/can be supplied to the build method
         */
        NONE,
        /**
         * A plain Jakarta Validator can be supplied. You must supply your own callback
         */
        JAKARTA_PLAIN,
        /**
         * Allow the supplying of Avaje's Validator
         */
        AVAJE
    }
    
    /**
     * How should we generate names?
     */
    public enum NameGeneration {
        /**
         * Do not generate names at all
         */
        NONE,
        /**
         * Generate names by matching what's been written
         */
        MATCH,
        /**
         * Generate names by using UpperCamelCase naming convention
         */
        UPPER_FIRST_LETTER
    }

    /**
     * How should diffs be evaluated?
     */
    public enum DiffEvaluationMode {
        /**
         * Diff is computed as soon as you call the "diff" method
         */
        EAGER,
        /**
         * Diff is computed when you make a request of a field.
         * <p>
         * Diffs are computed once
         */
        LAZY
    }
    //#endregion

    //#region Enclosed Element annotations

    /**
     * Indicate to the annotation processor which constructor is the one that should be
     *   used for building etc.
     * <p>
     * Only required if there are multiple constructors
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ CONSTRUCTOR })
    public @interface TargetConstructor {

    }

    //#endregion

    //#region Sub options

    /**
     * Options for the types being created (utils class, interface)
     * <p>
     * Note: this only impacts externally visible classes
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({})
    public @interface TypeNameOptions {
        /**
         * Suffix for the utility class created for e.g. Builder, Wither
         */
        String utilsImplementationSuffix() default "Utils";

        /**
         * Prefix for the utility class created for e.g. Builder, Wither
         */
        String utilsImplementationPrefix() default "";
    }

    /**
     * Options for the builder
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({})
    public @interface BuilderOptions {
        /**
         * Should builders create fluent versions (if possible)?
         */
        boolean fluent() default true;

        /**
         * The name of the builder within the Utils class. Will end up as e.g. {@code MyInterfaceUtils.Builder}
         */
        String builderName() default "Builder";

        /**
         * Name of the static no-arg creation of the builder
         */
        String emptyCreationName() default "builder";

        /**
         * Name of the static builder method that will copy an existing object passed to it
         */
        String copyCreationName() default "builder";

        /**
         * Name of the builder method that will copy an existing object passed to it.
         * <p>
         * Note that the build method defers to the getter method. This ensures that e.g. the correct collection type, is returned
         */
        String buildMethodName() default "build";

        /**
         * Should we build a validator that takes a Validator to validate the built object?
         * <p>
         * Requires the implementation requested
         */
        ValidationApi validatedBuilder() default ValidationApi.NONE;

        /**
         * For elements that are collections, do we create "add" methods?
         */
        boolean createAdderMethods() default true;

        /**
         * If creating adder methods, what is the prefix of the method name?
         */
        String adderMethodPrefix() default "add";

        /**
         * If creating adder methods, what is the suffix of the method name?
         */
        String adderMethodSuffix() default "";

        /**
         * For elements that are collections, do we create "remove" methods?
         */
        boolean createRemoveMethods() default true;

        /**
         * If creating remover methods, what is the prefix of the method name?
         */
        String removeMethodPrefix() default "remove";

        /**
         * If creating remover methods, what is the suffix of the method name?
         */
        String removeMethodSuffix() default "";

        /**
         * For elements that are collections, do we create "retainAll" methods?
         */
        boolean createRetainAllMethod() default true;

        /**
         * If creating retain methods, what is the prefix of the method name?
         */
        String retainMethodPrefix() default "retainAll";

        /**
         * If creating retain methods, what is the suffix of the method name?
         */
        String retainMethodSuffix() default "";

        /**
         * If the builder is supplied a "null" value and the existing value isn't null, should we replace the non-null value with null?
         */
        boolean nullReplacesNotNull() default true;

        /**
         * Should the builder generate explicit {@code setXToNull()} methods?
         * <p>
         * Notes:
         * <ul>
         *   <li>Only generated for reference types (non-primitives).</li>
         *   <li>When applied to collection components, this sets the builder field to {@code null};
         *       the final built value may still be an empty collection if
         *       {@link BuilderOptions#buildNullCollectionToEmpty()} is {@code true}.</li>
         *   <li>This is orthogonal to {@link BuilderOptions#nullReplacesNotNull()} — calling
         *       {@code setXToNull()} always sets the builder field to {@code null}.</li>
         * </ul>
         */
        boolean setToNullMethods() default false;

        /**
         * What type of collection should be built?
         */
        BuiltCollectionType builtCollectionType() default BuiltCollectionType.JAVA_IMMUTABLE;

        /**
         * When a collection is being set to null, instead make it an empty collection
         */
        boolean buildNullCollectionToEmpty() default true;

        /**
         * Should we create methods that set time objects to now?
         */
        boolean setTimeNowMethods() default true;

        /**
         * Prefix for setting time to now methods
         */
        String setTimeNowMethodPrefix() default "set";

        /**
         * Suffix for setting time to now methods when not enforcing time zones
         */
        String setTimeNowMethodSuffix() default "ToNow";

        /**
         * What is the name when setting a field when the target could be one of multiple types?
         * <p>
         * E.g. you have a field named {@code iface} that takes {@code YourInterface} which could be implemented by {@code YourRecordA} or {@code YourRecordB}. This
         *  will generate fluent setters named {@code ifaceAsYourRecordA} and {@code ifaceAsYourRecordB}
         */
        String multiTypeSetterBridge() default "As";

        /**
         * What is the name when setting a field when the target could be one of multiple types?
         * <p>
         * E.g. you have a collection field named {@code myCol} that takes {@code YourInterface} which could be implemented by {@code YourRecordA} or {@code YourRecordB}. This
         *  will generate fluent setters named {@code addYourRecordAToMyCol} and {@code addYourRecordBToMyCol}
         */
        String multiTypeAdderBridge() default "To";

        /**
         * Should we generate an overload for optional types that accepts the concrete one?
         */
        boolean concreteSettersForOptional() default true;
    }

    /**
     * Options for the wither
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({})
    public @interface WitherOptions {
        /**
         * Name of the interface generated
         */
        String witherName() default "With";

        /**
         * Prefix of the methods used by the wither
         */
        String withMethodPrefix() default "with";

        /**
         * Prefix of the methods used by the wither
         */
        String withMethodSuffix() default "";

        /**
         * Name of the method that converts the current object into a builder
         */
        String convertToBuilder() default "with";
    }

    /**
     * Options for the merger
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({})
    public @interface MergerOptions {
        /**
         * Name of the interface generated
         */
        String mergerName() default "Mergeable";

        /**
         * Name of the method to merge two instances
         */
        String mergerMethodName() default "merge";

        /**
         * Should static methods (if generated) be added to the root {@code Utils} class?
         * <p>
         * e.g. {@code PersonUtils.merge(preferredPerson, secondaryPerson)}
         */
        boolean staticMethodsAddedToUtils() default false;
    }

    /**
     * Options for the XML interface and utility generation
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({})
    public @interface XmlOptions {
        /**
         * Name of the interface generated
         */
        String xmlName() default "XML";

        /**
         * The name of the method on the interface that continues an already started XML document, and does not finish it
         */
        String continueAddingToXmlMethodName() default "writeSelfTo";

        /**
         * If we are meant to write a collection out and it has a wrapping element, and the collection is empty
         * do we write an empty element e.g. {@code <MyList />} (true) or don't write the wrapping element (false)
         */
        boolean writeEmptyCollectionsWithWrapperAsEmptyElement() default false;

        /**
         * Should we infer the presence of {@code @XmlElement} annotations?
         * <p>
         * The (default) value of "NONE" will cause compilation to fail if there is a missing XmlAnnotation 
         *   and it's not inferred from another item
         */
        NameGeneration inferXmlElementName() default NameGeneration.NONE;
    }
    
    /**
     * Options for the diff interface and utility generation
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({})
    public @interface DiffOptions {
        /**
         * Name of the interface generated
         */
        String differName() default "Diffable";

        /**
         * Name of the method to create a diff of two instances
         */
        String differMethodName() default "diff";

        /**
         * Prefix to use for the class name of the result of the diff
         */
        String diffResultPrefix() default "DiffOf";

        /**
         * Suffix to use for the class name of the result of the diff
         */
        String diffResultSuffix() default "";

        /**
         * The name given to the originating element.
         * <p>
         * Examples could be "original", "left"
         */
        String originatingElementName() default "original";

        /**
         * The name given to the other element.
         * <p>
         * Examples could be "updated", "right"
         */
        String comparedToElementName() default "updated";

        /**
         * The method name that will inform you if any field has changed at all
         */
        String changedAnyMethodName() default "hasChanged";

        /**
         * The prefix to a field when checking if there was a change
         */
        String changedCheckPrefix() default "has";

        /**
         * The suffix to a field when checking if there was a change
         */
        String changedCheckSuffix() default "Changed";

        /**
         * Should diffs be evaluated immediately or lazily?
         */
        DiffEvaluationMode evaluationMode() default DiffEvaluationMode.EAGER;

        /**
         * Should static methods (if generated) be added to the root {@code Utils} class?
         * <p>
         * e.g. {@code PersonUtils.diff(preferredPerson, secondaryPerson)}
         */
        boolean staticMethodsAddedToUtils() default false;
    }
    
    //#endregion

    // A default class type to be used internally (for when nothing has been set for a class field)
    @SuppressWarnings("java:S2094")
    public static class DEFAULT {}

    /**
     * An interface that requests the processor import "*Utils" classes from
     *   an existing library.
     * <p>
     * While this has the same functionality as the {@link AdvancedRecordUtils#importTargets()}
     *   the semantic naming makes the intent clearer
     */
    @Target({MODULE, PACKAGE, TYPE, ANNOTATION_TYPE})
    @Retention(RetentionPolicy.CLASS)
    public @interface ImportLibraryUtils {
        Class<? extends GeneratedUtil>[] value();
    }
}
