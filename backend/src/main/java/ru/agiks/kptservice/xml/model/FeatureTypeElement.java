package ru.agiks.kptservice.xml.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class FeatureTypeElement {

    @JacksonXmlElementWrapper(localName = "Attributes")
    @JacksonXmlProperty(localName = "Attribute")
    List<AttributeElement> attributes = new ArrayList<>();

    @JacksonXmlProperty(isAttribute = true, localName = "geometry_type")
    String geometryType;
}
