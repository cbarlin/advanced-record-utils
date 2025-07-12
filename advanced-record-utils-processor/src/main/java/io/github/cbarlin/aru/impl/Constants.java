package io.github.cbarlin.aru.impl;

import static io.github.cbarlin.aru.core.types.OperationType.CLASS;
import static io.github.cbarlin.aru.core.types.OperationType.FIELD_AND_ACCESSORS;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.micronaut.sourcegen.javapoet.ClassName;

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
        public static final ClaimableOperation DIFFER_COMPUTE_CHANGE = new ClaimableOperation("differComputation", FIELD_AND_ACCESSORS);
        public static final ClaimableOperation DIFFER_OVERALL_HAS_CHANGED = new ClaimableOperation("differGlobalHasChanged", CLASS);
        public static final ClaimableOperation DIFFER_IFACE = new ClaimableOperation("differInterface", CLASS);
        public static final ClaimableOperation DIFFER_RESULT = new ClaimableOperation("differResult", CLASS);
        public static final ClaimableOperation INTERNAL_MATCHING_IFACE = new ClaimableOperation("internalMatchingIface", CLASS);
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
        public static final ClaimableOperation XML_WRITE_FIELD = new ClaimableOperation("xmlWriteField", FIELD_AND_ACCESSORS);
    }
    
    public enum Names {
        ;
        private static final String XML_ANNOTATIONS = "jakarta.xml.bind.annotation";
        public static final ClassName AVAJE_CONSTRAINT_VIOLATION = ClassName.get("io.avaje.validation", "ConstraintViolationException");
        public static final ClassName AVAJE_JSONB_IMPORT = ClassName.get("io.avaje.jsonb", "Json", "Import");
        public static final ClassName AVAJE_VALIDATOR = ClassName.get("io.avaje.validation", "Validator");
        public static final ClassName BI_CONSUMER = ClassName.get(BiConsumer.class);
        public static final ClassName BIG_DECIMAL = ClassName.get("java.math", "BigDecimal");
        public static final ClassName BIG_INTEGER = ClassName.get("java.math", "BigInteger");
        public static final ClassName CHAR_SEQUENCE = ClassName.get(CharSequence.class);
        public static final ClassName CONSTRAINT_VIOLATION = ClassName.get("jakarta.validation", "ConstraintViolation");
        public static final ClassName CONSUMER = ClassName.get(Consumer.class);
        public static final ClassName DATE_TIME_FORMATTER = ClassName.get(DateTimeFormatter.class);
        public static final ClassName ILLEGAL_ARGUMENT_EXCEPTION = ClassName.get(IllegalArgumentException.class);
        public static final ClassName ITERABLE = ClassName.get(Iterable.class);
        public static final ClassName JAKARTA_VALIDATOR = ClassName.get("jakarta.validator", "Validator");
        public static final ClassName LOCAL_DATE_TIME = ClassName.get(LocalDateTime.class);
        public static final ClassName NON_NULL = CommonsConstants.Names.NON_NULL;
        public static final ClassName NOT_NULL = CommonsConstants.Names.NOT_NULL;
        public static final ClassName OBJECTS = CommonsConstants.Names.OBJECTS;
        public static final ClassName OFFSET_DATE_TIME = ClassName.get(OffsetDateTime.class);
        public static final ClassName PREDICATE = ClassName.get(Predicate.class);
        public static final ClassName STRING = ClassName.get(String.class);
        public static final ClassName STRINGUTILS = ClassName.get("org.apache.commons.lang3", "StringUtils");
        public static final ClassName TYPE_ALIAS = ClassName.get("io.github.cbarlin.aru.annotations", "TypeAlias");
        public static final ClassName UNARY_OPERATOR = ClassName.get(UnaryOperator.class);
        public static final ClassName VALIDATE = ClassName.get("org.apache.commons.lang3", "Validate");
        public static final ClassName XML_ATTRIBUTE = ClassName.get(XML_ANNOTATIONS, "XmlAttribute");
        public static final ClassName XML_ELEMENT = ClassName.get(XML_ANNOTATIONS, "XmlElement");
        public static final ClassName XML_ELEMENT_DEFAULT = XML_ELEMENT.nestedClass("DEFAULT");
        public static final ClassName XML_ELEMENT_WRAPPER = ClassName.get(XML_ANNOTATIONS, "XmlElementWrapper");
        public static final ClassName XML_ELEMENTS = ClassName.get(XML_ANNOTATIONS, "XmlElements");
        public static final ClassName XML_NAMESPACE_CONTEXT = ClassName.get("javax.xml.namespace", "NamespaceContext");
        public static final ClassName XML_ROOT_ELEMENT = ClassName.get(XML_ANNOTATIONS, "XmlRootElement");
        public static final ClassName XML_STREAM_EXCEPTION = ClassName.get("javax.xml.stream", "XMLStreamException");
        public static final ClassName XML_STREAM_WRITER = ClassName.get("javax.xml.stream", "XMLStreamWriter");
        public static final ClassName XML_TRANSIENT = ClassName.get(XML_ANNOTATIONS, "XmlTransient");
        public static final ClassName XML_TYPE = ClassName.get(XML_ANNOTATIONS, "XmlType");
        public static final ClassName ZONE_ID = ClassName.get(ZoneId.class);
        public static final ClassName ZONE_OFFSET = ClassName.get(ZoneOffset.class);
        public static final ClassName ZONED_DATE_TIME = ClassName.get(ZonedDateTime.class);

        // Use string based lookup since we can't import the "UUID" class when the constant name is "UUID"
        public static final ClassName UUID = ClassName.get("java.util", "UUID");
    }

    public enum InternalReferenceNames {
        ;
        public static final String INTERNAL_MATCHING_IFACE_NAME = "_MatchingInterface";
        public static final String MERGER_UTILS_CLASS = "_MergerUtils";
        public static final String XML_UTILS_CLASS = "_XmlUtils";
        public static final String XML_DEFAULT_STRING = "##default";
        public static final String XML_DEFAULT_TAG_NAME_VAR_NAME = "__DEFAULT_TAG_NAME";
        public static final String XML_DEFAULT_NAMESPACE_VAR_NAME = "__DEFAULT_NAMESPACE_URI";
        public static final String XML_PACKAGE_NAMESPACE_VAR_NAME = "__PKG_NAMESPACE_URI";
    }
}
