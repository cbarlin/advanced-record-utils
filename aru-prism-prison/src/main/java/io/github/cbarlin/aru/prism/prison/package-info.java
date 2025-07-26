// JsonB annotations
@GeneratePrism(publicAccess = true, value = Json.class, name = "JsonBPrism")
@GeneratePrism(publicAccess = true, value = Json.Raw.class, name = "JsonBRawPrism")
@GeneratePrism(publicAccess = true, value = Json.Alias.class, name = "JsonBAliasPrism")
@GeneratePrism(publicAccess = true, value = Json.Creator.class, name = "JsonBCreatorPrism")
@GeneratePrism(publicAccess = true, value = Json.Ignore.class, name = "JsonBIgnorePrism")
@GeneratePrism(publicAccess = true, value = Json.Unmapped.class, name = "JsonBUnmappedPrism")
@GeneratePrism(publicAccess = true, value = Json.Value.class, name = "JsonBValuePrism")
@GeneratePrism(publicAccess = true, value = Json.SubType.class, name = "JsonBSubTypePrism")
@GeneratePrism(publicAccess = true, value = Json.SubTypes.class, name = "JsonBSubTypesPrism")
@GeneratePrism(publicAccess = true, value = Json.Property.class, name = "JsonBPropertyPrism")
@GeneratePrism(publicAccess = true, value = CustomAdapter.class, name = "JsonBCustomAdaptorPrism")
// Jakarta XML
@GeneratePrism(publicAccess = true, value = XmlAttribute.class)
@GeneratePrism(publicAccess = true, value = XmlSchema.class)
@GeneratePrism(publicAccess = true, value = XmlNs.class)
@GeneratePrism(publicAccess = true, value = XmlJavaTypeAdapter.class)
@GeneratePrism(publicAccess = true, value = XmlJavaTypeAdapters.class)
@GeneratePrism(publicAccess = true, value = XmlRootElement.class)
@GeneratePrism(publicAccess = true, value = XmlType.class)
@GeneratePrism(publicAccess = true, value = XmlEnum.class)
@GeneratePrism(publicAccess = true, value = XmlEnumValue.class)
@GeneratePrism(publicAccess = true, value = XmlElement.class)
@GeneratePrism(publicAccess = true, value = XmlElements.class)
@GeneratePrism(publicAccess = true, value = XmlElementRef.class)
@GeneratePrism(publicAccess = true, value = XmlElementRefs.class)
@GeneratePrism(publicAccess = true, value = XmlElementWrapper.class)
@GeneratePrism(publicAccess = true, value = XmlAccessorOrder.class)
@GeneratePrism(publicAccess = true, value = XmlTransient.class)
@GeneratePrism(publicAccess = true, value = XmlValue.class)
@GeneratePrism(publicAccess = true, value = XmlSeeAlso.class)
// Jackson Annotations
@GeneratePrism(publicAccess = true, value = JsonAlias.class)
@GeneratePrism(publicAccess = true, value = JsonCreator.class)
@GeneratePrism(publicAccess = true, value = JsonFormat.class)
@GeneratePrism(publicAccess = true, value = JsonIgnore.class)
@GeneratePrism(publicAccess = true, value = JsonIgnoreProperties.class)
@GeneratePrism(publicAccess = true, value = JsonIgnoreType.class)
@GeneratePrism(publicAccess = true, value = JsonInclude.class)
@GeneratePrism(publicAccess = true, value = JsonIncludeProperties.class)
@GeneratePrism(publicAccess = true, value = JsonProperty.class)
@GeneratePrism(publicAccess = true, value = JsonSubTypes.class)
@GeneratePrism(publicAccess = true, value = JsonTypeId.class)
@GeneratePrism(publicAccess = true, value = JsonTypeInfo.class)
@GeneratePrism(publicAccess = true, value = JsonTypeName.class)
@GeneratePrism(publicAccess = true, value = JsonUnwrapped.class)
// Our own prisms!
@GeneratePrism(value = AdvancedRecordUtils.class, publicAccess = true)
@GeneratePrism(value = AdvancedRecordUtils.BuilderOptions.class, publicAccess = true)
@GeneratePrism(value = AdvancedRecordUtils.WitherOptions.class, publicAccess = true)
@GeneratePrism(value = AdvancedRecordUtils.MergerOptions.class, publicAccess = true)
@GeneratePrism(value = AdvancedRecordUtils.XmlOptions.class, publicAccess = true)
@GeneratePrism(value = AdvancedRecordUtils.DiffOptions.class, publicAccess = true)
@GeneratePrism(value = AdvancedRecordUtils.TargetConstructor.class, publicAccess = true)
@GeneratePrism(value = AdvancedRecordUtilsGenerated.class, publicAccess = true)
@GeneratePrism(value = AdvancedRecordUtilsGenerated.InternalUtil.class, publicAccess = true)
@GeneratePrism(value = AdvancedRecordUtilsGenerated.Version.class, publicAccess = true)
@GeneratePrism(value = io.github.cbarlin.aru.annotations.TypeConverter.class, publicAccess = true)
@GeneratePrism(value = io.github.cbarlin.aru.annotations.Generated.class, publicAccess = true)
package io.github.cbarlin.aru.prism.prison;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeId;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.Json;
import io.avaje.prism.GeneratePrism;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtilsGenerated;
import jakarta.xml.bind.annotation.XmlAccessorOrder;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlNs;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchema;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapters;