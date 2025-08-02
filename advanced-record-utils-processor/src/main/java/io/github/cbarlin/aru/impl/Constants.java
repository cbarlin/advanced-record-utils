package io.github.cbarlin.aru.impl;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.micronaut.sourcegen.javapoet.ClassName;

import static io.github.cbarlin.aru.core.types.OperationType.CLASS;
import static io.github.cbarlin.aru.core.types.OperationType.FIELD_AND_ACCESSORS;

public enum Constants {
    ;
    public enum Claims {
        ;
        public static final ClaimableOperation ALL_IFACE = new ClaimableOperation("allIface", CLASS);
        public static final ClaimableOperation BUILDER_ADD_ALL_ITERABLE = new ClaimableOperation("builderAddAllIterable", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation BUILDER_ADD_ALL_VARARGS = new ClaimableOperation("builderAddAllVarargs", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation BUILDER_ADD_VALIDATED_BUILD_METHOD = new ClaimableOperation("builderAddValidatedBuildMethod", CLASS);
        public static final ClaimableOperation BUILDER_ALIAS_SETTER = new ClaimableOperation("builderAliasSetter", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation BUILDER_CONCRETE_OPTIONAL = new ClaimableOperation("builderConcreteSetterForOptional", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation BUILDER_FLUENT_SETTER = new ClaimableOperation("builderFluentSetter", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation BUILDER_SET_TIME_TO_NOW = new ClaimableOperation("builderSetTimeToNow", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation BUILDER_USE_TYPE_CONVERTER = new ClaimableOperation("builderUseTypeConverter", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation DIFFER_COLLECTION_RESULT = new ClaimableOperation("differCollectionResult", CLASS);
        public static final ClaimableOperation DIFFER_COMPUTE_CHANGE = new ClaimableOperation("differComputation", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation DIFFER_IFACE = new ClaimableOperation("differInterface", CLASS);
        public static final ClaimableOperation DIFFER_OVERALL_HAS_CHANGED = new ClaimableOperation("differGlobalHasChanged", CLASS);
        public static final ClaimableOperation DIFFER_RESULT = new ClaimableOperation("differResult", CLASS);
        public static final ClaimableOperation DIFFER_STATIC_UTILS_METHOD = new ClaimableOperation("differStaticUtilsMethod", CLASS);
        public static final ClaimableOperation DIFFER_UTILS = new ClaimableOperation("differUtils", CLASS);
        public static final ClaimableOperation DIFFER_UTILS_COMPUTE_CHANGE = new ClaimableOperation("differUtilsComputation", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation DIFFER_VALUE_HOLDING = new ClaimableOperation("differValueHolding", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation INTERNAL_MATCHING_IFACE = new ClaimableOperation("internalMatchingIface", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation MERGE_IFACE_MERGE = new ClaimableOperation("mergeInterfaceMergeMethod", CLASS);
        public static final ClaimableOperation MERGE_IFACE_MERGE_OPTIONAL = new ClaimableOperation("mergeInterfaceMergeOptionalMethod", CLASS);
        public static final ClaimableOperation MERGE_STATIC_MERGE = new ClaimableOperation("mergeStaticMergeMethod", CLASS);
        public static final ClaimableOperation MERGER_ADD_FIELD_MERGER_METHOD = new ClaimableOperation("mergerAddFieldMergerMethod", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation MERGER_IFACE = new ClaimableOperation("mergerInterface", CLASS);
        public static final ClaimableOperation MERGER_STATIC_CLASS = new ClaimableOperation("mergerStaticClass", CLASS);
        public static final ClaimableOperation MISC_AVAJE_JSONB_IMPORT = new ClaimableOperation("miscAvajeJsonbImport", CLASS);
        public static final ClaimableOperation WITHER_FLUENT_BUILDER = new ClaimableOperation("witherFluentBuilder", CLASS);
        public static final ClaimableOperation WITHER_IFACE = new ClaimableOperation("wither", CLASS);
        public static final ClaimableOperation WITHER_TO_BUILDER = new ClaimableOperation("witherToBuilder", CLASS);
        public static final ClaimableOperation WITHER_USE_TYPE_CONVERTER = new ClaimableOperation("witherUseTypeConverter", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation WITHER_WITH = new ClaimableOperation("witherWith", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation WITHER_WITH_ADD = new ClaimableOperation("witherWithAdd", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation WITHER_WITH_ALIAS = new ClaimableOperation("witherWithAlias", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation WITHER_WITH_FLUENT = new ClaimableOperation("witherWithFluent", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation WITHER_WITH_OPTIONAL = new ClaimableOperation("witherWithOptional", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation XML_IFACE = new ClaimableOperation("xmlInterface", CLASS);
        public static final ClaimableOperation XML_IFACE_TO_XML = new ClaimableOperation("xmlInterfaceToXml", CLASS);
        public static final ClaimableOperation XML_IFACE_TO_XML_NO_DEF_NAMESPACE = new ClaimableOperation("xmlInterfaceToXmlNoDefNs", CLASS);
        public static final ClaimableOperation XML_IFACE_TO_XML_NO_NAMESPACE = new ClaimableOperation("xmlInterfaceToXmlNoNs", CLASS);
        public static final ClaimableOperation XML_IFACE_TO_XML_NOT_TAG = new ClaimableOperation("xmlInterfaceToXmlNoTag", CLASS);
        public static final ClaimableOperation XML_STATIC_CLASS = new ClaimableOperation("xmlStaticClass", CLASS);
        public static final ClaimableOperation XML_STATIC_CLASS_TO_XML = new ClaimableOperation("xmlStaticClassToXml", CLASS);
        public static final ClaimableOperation XML_STATIC_CLASS_TO_XML_NO_DEF_NAMESPACE = new ClaimableOperation("xmlStaticClassToXmlNoDefNS", CLASS);
        public static final ClaimableOperation XML_STATIC_CLASS_TO_XML_NO_NAMESPACE = new ClaimableOperation("xmlStaticClassToXmlNoNS", CLASS);
        public static final ClaimableOperation XML_STATIC_CLASS_TO_XML_NO_TAG = new ClaimableOperation("xmlStaticClassToXmlNoTag", CLASS);
        public static final ClaimableOperation XML_UNWRAP_OPTIONAL = new ClaimableOperation("xmlUnwrapOptional", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation XML_UNWRAP_TYPE_COMPONENT = new ClaimableOperation("xmlUnwrapTypeComponent", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation XML_WRITE_FIELD = new ClaimableOperation("xmlWriteField", FIELD_AND_ACCESSORS);
    }
    
    public enum Names {
        ;
        // ClassNames load faster when using the string constructor vs the .class one
        private static final String AVAJE_JSONB = "io.avaje.jsonb";
        private static final String JAVA_LANG = "java.lang";
        private static final String JAVA_TIME = "java.time";
        private static final String JAVA_UTIL = "java.util";
        private static final String JAVA_UTIL_FUNCTION = JAVA_UTIL + ".function";
        private static final String XML_ANNOTATIONS = "jakarta.xml.bind.annotation";
        public static final ClassName AVAJE_CONSTRAINT_VIOLATION = ClassName.get("io.avaje.validation", "ConstraintViolationException");
        public static final ClassName AVAJE_JSONB_IGNORE = ClassName.get(AVAJE_JSONB, "Json", "Ignore");
        public static final ClassName AVAJE_JSONB_IMPORT = ClassName.get(AVAJE_JSONB, "Json", "Import");
        public static final ClassName AVAJE_JSONB_SUBTYPE = ClassName.get(AVAJE_JSONB, "Json", "SubType");
        public static final ClassName AVAJE_VALIDATOR = ClassName.get("io.avaje.validation", "Validator");
        public static final ClassName BI_CONSUMER = ClassName.get(JAVA_UTIL_FUNCTION, "BiConsumer");
        public static final ClassName BIG_DECIMAL = ClassName.get("java.math", "BigDecimal");
        public static final ClassName BIG_INTEGER = ClassName.get("java.math", "BigInteger");
        public static final ClassName CHAR_SEQUENCE = ClassName.get(JAVA_LANG, "CharSequence");
        public static final ClassName COLLECTIONS = ClassName.get(JAVA_UTIL, "Collections");
        public static final ClassName CONSTRAINT_VIOLATION = ClassName.get("jakarta.validation", "ConstraintViolation");
        public static final ClassName CONSUMER = ClassName.get(JAVA_UTIL_FUNCTION, "Consumer");
        public static final ClassName DATE_TIME_FORMATTER = ClassName.get(JAVA_TIME + ".format","DateTimeFormatter");
        public static final ClassName ENUM_SET = ClassName.get(JAVA_UTIL, "EnumSet");
        public static final ClassName FUNCTION = ClassName.get(JAVA_UTIL_FUNCTION, "Function");
        public static final ClassName ILLEGAL_ARGUMENT_EXCEPTION = ClassName.get(JAVA_LANG, "IllegalArgumentException");
        public static final ClassName ITERABLE = ClassName.get(JAVA_LANG, "Iterable");
        public static final ClassName ITERATOR = ClassName.get(JAVA_UTIL, "Iterator");
        public static final ClassName JAKARTA_VALIDATOR = ClassName.get("jakarta.validator", "Validator");
        public static final ClassName LINKED_LIST = ClassName.get(JAVA_UTIL, "LinkedList");
        public static final ClassName LIST = CommonsConstants.Names.LIST;
        public static final ClassName LOCAL_DATE_TIME = ClassName.get(JAVA_TIME, "LocalDateTime");
        public static final ClassName LONG = ClassName.get(JAVA_LANG, "Long");
        public static final ClassName MAP = ClassName.get(JAVA_UTIL, "Map");
        public static final ClassName MATH = ClassName.get(JAVA_LANG, "Math");
        public static final ClassName NON_NULL = CommonsConstants.Names.NON_NULL;
        public static final ClassName NOT_NULL = CommonsConstants.Names.NOT_NULL;
        public static final ClassName OBJECTS = CommonsConstants.Names.OBJECTS;
        public static final ClassName OFFSET_DATE_TIME = ClassName.get(JAVA_TIME, "OffsetDateTime");
        public static final ClassName PREDICATE = ClassName.get(JAVA_UTIL_FUNCTION, "Predicate");
        public static final ClassName SET = CommonsConstants.Names.SET;
        public static final ClassName SORTED_SET = ClassName.get(JAVA_UTIL, "SortedSet");
        public static final ClassName SPLITERATOR = ClassName.get(JAVA_UTIL, "Spliterator");
        public static final ClassName STACK = ClassName.get(JAVA_UTIL, "Stack");
        public static final ClassName STRING = ClassName.get(JAVA_LANG, "String");
        public static final ClassName STRINGUTILS = ClassName.get("org.apache.commons.lang3", "StringUtils");
        public static final ClassName TREE_SET = ClassName.get(JAVA_UTIL, "TreeSet");
        public static final ClassName TYPE_ALIAS = ClassName.get("io.github.cbarlin.aru.annotations", "TypeAlias");
        public static final ClassName UNARY_OPERATOR = ClassName.get(JAVA_UTIL_FUNCTION, "UnaryOperator");
        public static final ClassName UUID = ClassName.get(JAVA_UTIL, "UUID");
        public static final ClassName VALIDATE = ClassName.get("org.apache.commons.lang3", "Validate");
        public static final ClassName VECTOR = ClassName.get(JAVA_UTIL, "Vector");
        public static final ClassName XML_ATTRIBUTE = ClassName.get(XML_ANNOTATIONS, "XmlAttribute");
        public static final ClassName XML_ELEMENT = ClassName.get(XML_ANNOTATIONS, "XmlElement");
        public static final ClassName XML_ELEMENT_DEFAULT = XML_ELEMENT.nestedClass("DEFAULT");
        public static final ClassName XML_ELEMENT_WRAPPER = ClassName.get(XML_ANNOTATIONS, "XmlElementWrapper");
        public static final ClassName XML_ELEMENTS = ClassName.get(XML_ANNOTATIONS, "XmlElements");
        public static final ClassName XML_NAMESPACE_CONTEXT = ClassName.get("javax.xml.namespace", "NamespaceContext");
        public static final ClassName XML_ROOT_ELEMENT = ClassName.get(XML_ANNOTATIONS, "XmlRootElement");
        public static final ClassName XML_SCHEMA = ClassName.get(XML_ANNOTATIONS, "XmlSchema");
        public static final ClassName XML_STREAM_EXCEPTION = ClassName.get("javax.xml.stream", "XMLStreamException");
        public static final ClassName XML_STREAM_WRITER = ClassName.get("javax.xml.stream", "XMLStreamWriter");
        public static final ClassName XML_TRANSIENT = ClassName.get(XML_ANNOTATIONS, "XmlTransient");
        public static final ClassName XML_TYPE = ClassName.get(XML_ANNOTATIONS, "XmlType");
        public static final ClassName ZONE_ID = ClassName.get(JAVA_TIME, "ZoneId");
        public static final ClassName ZONE_OFFSET = ClassName.get(JAVA_TIME, "ZoneOffset");
        public static final ClassName ZONED_DATE_TIME = ClassName.get(JAVA_TIME, "ZonedDateTime");
    }

    public enum InternalReferenceNames {
        ;
        public static final String DIFFER_UTILS_CLASS = "_DifferUtils";
        public static final String INTERNAL_MATCHING_IFACE_NAME = "_MatchingInterface";
        public static final String MERGER_UTILS_CLASS = "_MergerUtils";
        public static final String XML_UTILS_CLASS = "_XmlUtils";
        public static final String XML_DEFAULT_STRING = "##default";
        public static final String XML_DEFAULT_TAG_NAME_VAR_NAME = "__DEFAULT_TAG_NAME";
        public static final String XML_DEFAULT_NAMESPACE_VAR_NAME = "__DEFAULT_NAMESPACE_URI";
        public static final String XML_PACKAGE_NAMESPACE_VAR_NAME = "__PKG_NAMESPACE_URI";
    }
}
