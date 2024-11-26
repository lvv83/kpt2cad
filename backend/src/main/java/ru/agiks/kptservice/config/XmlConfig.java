package ru.agiks.kptservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.agiks.kptservice.xml.SimpleFeatureFactory;
import ru.rkomi.kpt2cad.GeotoolsTransformationEngine;
import ru.rkomi.kpt2cad.xslt.CoordinateSystemDictionary;

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

    @Bean
    SimpleFeatureFactory simpleFeatureFactory(GeometryFactory geometryFactory, GeotoolsTransformationEngine transformationEngine, CoordinateSystemDictionary coordinateSystemDictionary)
    {
        // Создаём и настраиваем нашу фабрику
        SimpleFeatureFactory factory = new SimpleFeatureFactory(geometryFactory, transformationEngine, coordinateSystemDictionary);

        // default: false
        factory.setUseSRID(true); // проверьте наличие пакета gt-epsg-hsql в зависимостях при включении этой опции !!!

        // default: the_geom
        //factory.setGeometryPropertyName("the_geom");

        // default: 4326
        //factory.setSRID(4326);

        // default: cad_qrtr
        //factory.setCadastralQuarterPropertyName("cad_qrtr");

        return factory;
    }
}
