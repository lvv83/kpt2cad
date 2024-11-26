package ru.agiks.kptservice.xml.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class HoleElement {
    @JacksonXmlProperty(localName = "Coordinate")
    List<CoordinateElement> coordinates = new ArrayList<>();
}
