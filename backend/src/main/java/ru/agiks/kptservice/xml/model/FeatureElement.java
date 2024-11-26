package ru.agiks.kptservice.xml.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class FeatureElement {
    @JacksonXmlElementWrapper(localName = "Attributes")
    @JacksonXmlProperty(localName = "Attribute")
    List<AttributeElement> attributes = new ArrayList<>();

    @JacksonXmlProperty(localName = "Geometry")
    GeometryElement geometry;

    public Optional<AttributeElement> getAttributeByName(String name)
    {
        return attributes.stream().filter(x -> name.equals(x.getName())).findFirst();
    }
}
