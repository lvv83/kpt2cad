package ru.agiks.kptservice.xml;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.agiks.kptservice.xml.model.*;
import ru.rkomi.kpt2cad.Proj4DemoApplication;

import javax.xml.stream.XMLInputFactory;
import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

// Указываем ссылку на главный класс приложения здесь, так как имя пакета отличается
@SpringBootTest(classes = Proj4DemoApplication.class)
public class XmlDeserializationTest {

    @Autowired
    XmlMapper xmlMapper;

    @Autowired
    XMLInputFactory xmlInputFactory;

    @Test
    void contextLoads() {
        // убедимся, что основная конфигурация приложения из другого пакета подтянулась
        assertThat(xmlMapper).isNotNull();
    }

    private File getResourcePath(String resource)
    {
        String path = getClass().getResource(resource).getFile();
        return new File(path);
    }

    @Test
    void bufferDeserializationTest()
    {
        // Десериализация через буфер
        // ==========================

        // Весь документ полностью считывается в память, раскладывается по объектам
        // На выходе получаем объект типа ShapeFileDocument, через который можно добраться до всех остальных.
        // Способ не рекомендуется при обработке очень больших файлов, приведён в качестве примера

        ShapeFileDocument sfd;

        try {
            File file = getResourcePath("/xsl/out.xml");
            sfd = xmlMapper.readValue(file, ShapeFileDocument.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertThat(sfd.getShapeFiles()).hasSize(1);

        ShapeFileElement shapeFile = sfd.getShapeFiles().get(0);
        assertThat(shapeFile.getPrefix()).isEqualTo("ZU");

        FeatureTypeElement featureType = shapeFile.getFeatureType();

        assertThat(featureType.getGeometryType()).isEqualTo("MultiPolygon");
        assertThat(featureType.getAttributes()).hasSize(9);

        AttributeElement attr = featureType.getAttributes().get(3);
        assertThat(attr.getName()).isEqualTo("cad_qrtr");
        assertThat(attr.getType()).isEqualTo("String");

        assertThat(shapeFile.getFeatures()).isNotEmpty();

        FeatureElement feature = shapeFile.getFeatures().get(0);

        AttributeElement featAttr = feature.getAttributes().get(2);
        assertThat(featAttr.getName()).isEqualTo("cad_num");
        assertThat(featAttr.getValue()).isEqualTo("11:19:0801020:1");

        assertThat(feature.getGeometry().getShells()).hasSize(1);

        ShellElement shell = feature.getGeometry().getShells().get(0);
        assertThat(shell.getCoordinates()).hasSize(9);

        CoordinateElement coordinate = shell.getCoordinates().get(0);
        assertThat(coordinate.getX()).isEqualTo(5296168.93, withPrecision(2d));
        assertThat(coordinate.getY()).isEqualTo(844316.56, withPrecision(2d));
    }

    @Test
    void streamTest()
    {
        // Потоковая десериализация
        // ========================

        // Потоковая десериализация выполняется посредством класса XmlFeatureReader и классом, реализующим
        // интерфейс XmlFeatureListener. Первый последовательно считывает из XML-файла элементы, десериализует их и
        // передаёт второму. Реализация XmlFeatureListener отвечает за дальнейшую обработку.
        // Например, здесь в качестве теста ведётся подсчёт количества записей и атрибутов


        // Получаем путь к файлу
        File xmlFile = getResourcePath("/xsl/out.xml");

        // Создаём и настраиваем XmlFeatureReader
        XmlFeatureReader reader = new XmlFeatureReader(xmlInputFactory, xmlFile);

        SimpleXmlFeatureListener listener = new SimpleXmlFeatureListener();
        reader.setListener(listener); // задаём экземпляр обработчика потока
        reader.setMapper(xmlMapper); // задаём экземпляр XML-десериализатора

        // Запускаем потоковую десериализацию
        reader.read();

        // Проверяем результаты через обработчик
        assertThat(listener.getAttributeCount()).isEqualTo(9);
        assertThat(listener.getPrefix()).isEqualTo("ZU");
        assertThat(listener.getFeatureCount()).isEqualTo(270);
    }

    @Getter
    static class SimpleXmlFeatureListener implements XmlFeatureListener
    {
        private int attributeCount = 0;

        private int featureCount = 0;

        private String prefix;

        @Override
        public void onFeatureType(FeatureTypeElement featureType) {
            attributeCount = featureType.getAttributes().size();
        }

        @Override
        public void onFeature(FeatureElement feature) {
            featureCount++;
        }

        @Override
        public void onShapeFilePrefix(String prefix) {
            this.prefix = prefix;
        }
    }
}
