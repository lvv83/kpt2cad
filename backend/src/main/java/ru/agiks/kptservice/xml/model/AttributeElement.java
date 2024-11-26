package ru.agiks.kptservice.xml.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.Getter;

@Getter
public class AttributeElement {

    @JacksonXmlProperty(isAttribute = true)
    String name;

    @JacksonXmlProperty(isAttribute = true)
    String type;

    @JacksonXmlText
    String value;

    /**
     * Преобразует значение поля "type" к короткому имени типа данных Java.
     * Регистр букв важен!
     * @return Имя типа данных
     */
    public String toSchemaType()
    {
        if (type == null)
            throw new RuntimeException("type should be set for this operation");

        return switch (type) {
            case "String" -> "String";
            case "Double", "Number" -> "Double";
            default -> throw new RuntimeException("Unsupported type " + type);
        };
    }

    public Object toObjectValue(String schemaType)
    {
        if (value == null)
            return null;

        return schemaType.equals("String") ? value : Double.valueOf(value);
    }
}
