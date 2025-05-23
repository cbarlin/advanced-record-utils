package io.github.cbarlin.aru.tests.c_odd_types;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import io.github.cbarlin.aru.tests.xml_util.ConvertToXml;

class NumericsTest {

    private static final byte PRIMITIVE_BYTE_VALUE = 69;
    private static final Byte BOXED_BYTE_VALUE = Byte.valueOf(PRIMITIVE_BYTE_VALUE);

    private static final double PRIMITIVE_DOUBLE_VALUE = 69d;
    private static final Double BOXED_DOUBLE_VALUE = Double.valueOf(PRIMITIVE_DOUBLE_VALUE);

    private static final int PRIMITIVE_INT_VALUE = 69;
    private static final Integer BOXED_INT_VALUE = Integer.valueOf(PRIMITIVE_INT_VALUE);

    private static final char PRIMITIVE_CHAR_VALUE = 69;
    private static final Character BOXED_CHAR_VALUE = Character.valueOf(PRIMITIVE_CHAR_VALUE);

    private static final long PRIMITIVE_LONG_VALUE = 69;
    private static final Long BOXED_LONG_VALUE = Long.valueOf(PRIMITIVE_LONG_VALUE);

    private static final float PRIMITIVE_FLOAT_VALUE = 69;
    private static final Float BOXED_FLOAT_VALUE = Float.valueOf(PRIMITIVE_FLOAT_VALUE);

    private static final short PRIMITIVE_SHORT_VALUE = 69;
    private static final Short BOXED_SHORT_VALUE = Short.valueOf(PRIMITIVE_SHORT_VALUE);

    private static final BigInteger BIG_INTEGER = BigInteger.TEN;
    private static final BigDecimal BIG_DECIMAL = BigDecimal.TEN;

    NumericsBag buildNoDefaults() {
        return NumericsBagUtils.builder()
            // byte
            .primitiveByteAttribute(PRIMITIVE_BYTE_VALUE)
            .primitiveByteElement(PRIMITIVE_BYTE_VALUE)
            .boxedByteAttribute(BOXED_BYTE_VALUE)
            .boxedByteAttributeRequired(BOXED_BYTE_VALUE)
            .boxedByteElement(BOXED_BYTE_VALUE)
            .boxedByteElementRequired(BOXED_BYTE_VALUE)
            // char
            .primitiveCharAttribute(PRIMITIVE_CHAR_VALUE)
            .primitiveCharElement(PRIMITIVE_CHAR_VALUE)
            .boxedCharAttribute(BOXED_CHAR_VALUE)
            .boxedCharAttributeRequired(BOXED_CHAR_VALUE)
            .boxedCharElement(BOXED_CHAR_VALUE)
            .boxedCharElementRequired(BOXED_CHAR_VALUE)
            // double
            .primitiveDoubleAttribute(PRIMITIVE_DOUBLE_VALUE)
            .primitiveDoubleElement(PRIMITIVE_DOUBLE_VALUE)
            .boxedDoubleAttribute(BOXED_DOUBLE_VALUE)
            .boxedDoubleAttributeRequired(BOXED_DOUBLE_VALUE)
            .boxedDoubleElement(BOXED_DOUBLE_VALUE)
            .boxedDoubleElementRequired(BOXED_DOUBLE_VALUE)
            // int
            .primitiveIntAttribute(PRIMITIVE_INT_VALUE)
            .primitiveIntElement(PRIMITIVE_INT_VALUE)
            .boxedIntAttribute(BOXED_INT_VALUE)
            .boxedIntAttributeRequired(BOXED_INT_VALUE)
            .boxedIntElement(BOXED_INT_VALUE)
            .boxedIntElementRequired(BOXED_INT_VALUE)
            // long
            .primitiveLongAttribute(PRIMITIVE_LONG_VALUE)
            .primitiveLongElement(PRIMITIVE_LONG_VALUE)
            .boxedLongAttribute(BOXED_LONG_VALUE)
            .boxedLongAttributeRequired(BOXED_LONG_VALUE)
            .boxedLongElement(BOXED_LONG_VALUE)
            .boxedLongElementRequired(BOXED_LONG_VALUE)
            // float
            .primitiveFloatAttribute(PRIMITIVE_FLOAT_VALUE)
            .primitiveFloatElement(PRIMITIVE_FLOAT_VALUE)
            .boxedFloatAttribute(BOXED_FLOAT_VALUE)
            .boxedFloatAttributeRequired(BOXED_FLOAT_VALUE)
            .boxedFloatElement(BOXED_FLOAT_VALUE)
            .boxedFloatElementRequired(BOXED_FLOAT_VALUE)
            // short
            .primitiveShortAttribute(PRIMITIVE_SHORT_VALUE)
            .primitiveShortElement(PRIMITIVE_SHORT_VALUE)
            .boxedShortAttribute(BOXED_SHORT_VALUE)
            .boxedShortAttributeRequired(BOXED_SHORT_VALUE)
            .boxedShortElement(BOXED_SHORT_VALUE)
            .boxedShortElementRequired(BOXED_SHORT_VALUE)
            // BigInteger
            .bigIntegerAttribute(BIG_INTEGER)
            .bigIntegerAttributeRequired(BIG_INTEGER)
            .bigIntegerElement(BIG_INTEGER)
            .bigIntegerElementRequired(BIG_INTEGER)
            // BigDecimal
            .bigDecimalAttribute(BIG_DECIMAL)
            .bigDecimalAttributeRequired(BIG_DECIMAL)
            .bigDecimalElement(BIG_DECIMAL)
            .bigDecimalElementRequired(BIG_DECIMAL)
            // OptionalInt
            .optionalIntAttributeRequired(10)
            // Boolean
            .boxedBool(Boolean.TRUE)
            .boxedBoolRequired(Boolean.FALSE)
            .build();
    }

    NumericsBag buildWithDefaults() {
        return buildNoDefaults().with()
            // byte
            .boxedByteElementDefault(BOXED_BYTE_VALUE)
            .boxedByteElementRequiredDefault(BOXED_BYTE_VALUE)
            // char
            .boxedCharElementDefault(BOXED_CHAR_VALUE)
            .boxedCharElementRequiredDefault(BOXED_CHAR_VALUE)
            // double
            .boxedDoubleElementDefault(BOXED_DOUBLE_VALUE)
            .boxedDoubleElementRequiredDefault(BOXED_DOUBLE_VALUE)
            // int
            .boxedIntElementDefault(BOXED_INT_VALUE)
            .boxedIntElementRequiredDefault(BOXED_INT_VALUE)
            // long
            .boxedLongElementDefault(BOXED_LONG_VALUE)
            .boxedLongElementRequiredDefault(BOXED_LONG_VALUE)
            // float
            .boxedFloatElementDefault(BOXED_FLOAT_VALUE)
            .boxedFloatElementRequiredDefault(BOXED_FLOAT_VALUE)
            // short
            .boxedShortElementDefault(BOXED_SHORT_VALUE)
            .boxedShortElementRequiredDefault(BOXED_SHORT_VALUE)
            // BigInteger
            .bigIntegerElementDefault(BIG_INTEGER)
            .bigIntegerElementRequiredDefault(BIG_INTEGER)
            // BigDecimal
            .bigDecimalElementDefault(BIG_DECIMAL)
            .bigDecimalElementRequiredDefault(BIG_DECIMAL)
            // Optional Int
            .optionalIntAttribute(10)
            .optionalIntElementDefaultNs(10)
            .optionalIntElementDefault(10)
            // Boolean

            .build();
    }

    @Test
    void xmlNoDefaults() {
        ConvertToXml.compareXml(out -> assertDoesNotThrow(() -> buildNoDefaults().writeSelfTo(out)), "expected_numerics_no_defaults.xml");
    }

    @Test
    void xmlWithDefaults() {
        ConvertToXml.compareXml(out -> assertDoesNotThrow(() -> buildWithDefaults().writeSelfTo(out)), "expected_numerics_with_defaults.xml");
    }

}
