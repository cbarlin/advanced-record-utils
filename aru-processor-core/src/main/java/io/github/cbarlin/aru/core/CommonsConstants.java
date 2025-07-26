package io.github.cbarlin.aru.core;

import static io.github.cbarlin.aru.core.types.OperationType.CLASS;
import static io.github.cbarlin.aru.core.types.OperationType.FIELD_AND_ACCESSORS;

import io.micronaut.sourcegen.javapoet.ClassName;

/**
 * Hold all the constant values across the processor
 */
public enum CommonsConstants {
    ;
    public static final String JDOC_PARA = "\n<p>\n";
    public enum Claims {
        ;
        // "Core" components everything should decorate or delegate to
        public static final ClaimableOperation CORE_BUILDER_CLASS = new ClaimableOperation("builder", CLASS);
        public static final ClaimableOperation CORE_BUILDER_FIELD = new ClaimableOperation("builderField", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation CORE_BUILDER_GETTER = new ClaimableOperation("builderGetter", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation CORE_BUILDER_SETTER = new ClaimableOperation("builderPlainSetter", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation CORE_BUILDER_SINGLE_ITEM_ADDER = new ClaimableOperation("builderAdd", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation CORE_BUILDER_FROM_NOTHING = new ClaimableOperation("builderEmpty", CLASS);
        // Everything else - these should either directly delegate to core methods/classes, or should have some sane reason as to not
        //   (e.g. an optimised "addAll" method on the builder if we know the type of collection being used)
        public static final ClaimableOperation BUILDER_BUILD = new ClaimableOperation("builderBuild", CLASS);
        public static final ClaimableOperation BUILDER_FROM_EXISTING = new ClaimableOperation("builderCopy", CLASS);
    }

    public enum Names {
        ;
        // ClassNames load faster when using the string constructor vs the .class one
        private static final String ADVANCED_RECORD_UTILS_GENERATED = "AdvancedRecordUtilsGenerated";
        private static final String IO_GITHUB_CBARLIN_ARU_ANNOTATIONS = "io.github.cbarlin.aru.annotations";
        private static final String JAKARTA_XML_BIND = "jakarta.xml.bind.annotation";
        private static final String JAVA_UTIL = "java.util";
        private static final String ORG_JSPECIFY_ANNOTATIONS = "org.jspecify.annotations";
        public static final ClassName ARRAY_LIST = ClassName.get(JAVA_UTIL, "ArrayList");
        public static final ClassName ARU_GENERATED = ClassName.get(IO_GITHUB_CBARLIN_ARU_ANNOTATIONS, ADVANCED_RECORD_UTILS_GENERATED);
        public static final ClassName ARU_INTERNAL_UTILS = ClassName.get(IO_GITHUB_CBARLIN_ARU_ANNOTATIONS, ADVANCED_RECORD_UTILS_GENERATED, "InternalUtil");
        public static final ClassName ARU_LOGGING_CONSTANTS = ClassName.get(IO_GITHUB_CBARLIN_ARU_ANNOTATIONS, "LoggingConstants");
        public static final ClassName ARU_MAIN_ANNOTATION = ClassName.get(IO_GITHUB_CBARLIN_ARU_ANNOTATIONS, "AdvancedRecordUtils");
        public static final ClassName ARU_VERSION = ClassName.get(IO_GITHUB_CBARLIN_ARU_ANNOTATIONS, ADVANCED_RECORD_UTILS_GENERATED, "Version");
        public static final ClassName COLLECTION = ClassName.get(JAVA_UTIL, "Collection");
        public static final ClassName COLLECTORS = ClassName.get(JAVA_UTIL + ".stream", "Collectors");
        public static final ClassName GENERATED_ANNOTATION = ClassName.get(IO_GITHUB_CBARLIN_ARU_ANNOTATIONS, "Generated");
        public static final ClassName GENERATED_UTIL = ClassName.get(IO_GITHUB_CBARLIN_ARU_ANNOTATIONS, "GeneratedUtil");
        public static final ClassName HASH_SET = ClassName.get(JAVA_UTIL, "HashSet");
        public static final ClassName LIST = ClassName.get(JAVA_UTIL, "List");
        public static final ClassName LOGGER = ClassName.get("org.slf4j", "Logger");
        public static final ClassName LOGGER_FACTORY = ClassName.get("org.slf4j", "LoggerFactory");
        public static final ClassName NON_NULL = ClassName.get(ORG_JSPECIFY_ANNOTATIONS, "NonNull");
        public static final ClassName NULL_MARKED = ClassName.get(ORG_JSPECIFY_ANNOTATIONS, "NullMarked");
        public static final ClassName NULL_UNMARKED = ClassName.get(ORG_JSPECIFY_ANNOTATIONS, "NullUnmarked");
        public static final ClassName NULLABLE = ClassName.get(ORG_JSPECIFY_ANNOTATIONS, "Nullable");
        public static final ClassName OBJECTS = ClassName.get(JAVA_UTIL, "Objects");
        public static final ClassName OPTIONAL = ClassName.get(JAVA_UTIL, "Optional");
        public static final ClassName OPTIONAL_DOUBLE = ClassName.get(JAVA_UTIL, "OptionalDouble");
        public static final ClassName OPTIONAL_INT = ClassName.get(JAVA_UTIL, "OptionalInt");
        public static final ClassName OPTIONAL_LONG = ClassName.get(JAVA_UTIL, "OptionalLong");
        public static final ClassName QUEUE = ClassName.get(JAVA_UTIL, "Queue");
        public static final ClassName SET = ClassName.get(JAVA_UTIL, "Set");
        public static final ClassName TYPE_ALIAS = ClassName.get(IO_GITHUB_CBARLIN_ARU_ANNOTATIONS, "TypeAlias");
        public static final ClassName UNSUPPORTED_OPERATION_EXCEPTION = ClassName.get("java.lang", "UnsupportedOperationException");
        public static final ClassName XML_ELEMENTS = ClassName.get(JAKARTA_XML_BIND, "XmlElements");
        public static final ClassName XML_SEE_ALSO = ClassName.get(JAKARTA_XML_BIND, "XmlSeeAlso");


        // Cross references the above. Used to resolve confusion
        public static final ClassName NOT_NULL = NON_NULL;
    }

    public enum InternalReferenceNames {
        ;
        public static final String ENSURE_MUTABLE_PREFIX = "__ensureMutable";
        public static final String LOGGER_NAME = "__LOGGER";
    }

}
