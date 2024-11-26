package ru.agiks.kptservice.xml.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ShapeFileElement {

    @JacksonXmlProperty(isAttribute = true)
    String prefix;

    @JacksonXmlProperty(localName = "FeatureType")
    FeatureTypeElement featureType;

    @JacksonXmlElementWrapper(localName = "Features")
    @JacksonXmlProperty(localName = "Feature")
    List<FeatureElement> features = new ArrayList<>();
}
