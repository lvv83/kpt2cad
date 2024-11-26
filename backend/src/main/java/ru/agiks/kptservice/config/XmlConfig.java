package ru.agiks.kptservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.stream.XMLInputFactory;

@Configuration
public class XmlConfig {

    @Bean
    XmlMapper xmlMapper()
    {
        // По умолчанию XmlMapper оборачивает коллекции в дополнительный элемент с именем, совпадающим с
        // элементом коллекции, что выглядит как дублирование.
        // Запретим это поведение, обёртка должна появляться только если это указано явно.

        // Кроме того, разрешим появление элементов и атрибутов, которые не прописаны в аннотациях

        return XmlMapper.builder()
                .defaultUseWrapper(false)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    @Bean
    XMLInputFactory xmlInputFactory()
    {
        return XMLInputFactory.newInstance();
    }
}
