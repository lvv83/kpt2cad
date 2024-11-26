package ru.agiks.kptservice.xml.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "ShapeFiles")
@Getter
public class ShapeFileDocument {

    @JacksonXmlProperty(localName = "ShapeFile")
    List<ShapeFileElement> shapeFiles = new ArrayList<>();
}