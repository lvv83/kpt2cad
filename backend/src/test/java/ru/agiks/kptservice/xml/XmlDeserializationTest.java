package ru.agiks.kptservice.xml;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.agiks.kptservice.xml.model.*;
import ru.rkomi.kpt2cad.Proj4DemoApplication;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

// Указываем ссылку на главный класс приложения здесь, так как имя пакета отличается
@SpringBootTest(classes = Proj4DemoApplication.class)
public class XmlDeserializationTest {

    @Autowired
    XmlMapper xmlMapper;

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
}
