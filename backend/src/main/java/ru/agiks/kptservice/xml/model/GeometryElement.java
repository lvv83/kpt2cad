package ru.agiks.kptservice.xml.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GeometryElement {
    @JacksonXmlProperty(localName = "Shell")
    List<ShellElement> shells = new ArrayList<>();
}
