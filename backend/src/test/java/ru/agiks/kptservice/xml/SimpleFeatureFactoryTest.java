package ru.agiks.kptservice.xml;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.SneakyThrows;
import org.geotools.api.feature.simple.SimpleFeature;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.agiks.kptservice.xml.model.FeatureElement;
import ru.agiks.kptservice.xml.model.FeatureTypeElement;
import ru.rkomi.kpt2cad.Proj4DemoApplication;


import static org.assertj.core.api.Assertions.assertThat;

// Указываем ссылку на главный класс приложения здесь, так как имя пакета отличается
@SpringBootTest(classes = Proj4DemoApplication.class)
public class SimpleFeatureFactoryTest {

    @Autowired
    SimpleFeatureFactory simpleFeatureFactory;

    @Autowired
    XmlMapper xmlMapper;

    @Test
    void contextLoads() {
        assertThat(simpleFeatureFactory).isNotNull();
    }

    @Test
    @SneakyThrows
    void testTransform() {
        // При помощи xmlMapper десериализуем элементы, а затем соберём из них объект типа SimpleFeature

        FeatureTypeElement featureTypeElement = xmlMapper.readValue("""
                        <FeatureType geometry_type="MultiPolygon">
                            <Attributes>
                                <Attribute name="src_file" type="String"/>
                                <Attribute name="DateUpload" type="String"/>
                                <Attribute name="cad_num" type="String"/>
                                <Attribute name="cad_qrtr" type="String"/>
                                <Attribute name="area" type="String"/>
                                <Attribute name="sk_id" type="String"/>
                                <Attribute name="category" type="String"/>
                                <Attribute name="permit_use" type="String"/>
                                <Attribute name="address" type="String"/>
                            </Attributes>
                        </FeatureType>
                """, FeatureTypeElement.class);

        FeatureElement featureElement = xmlMapper.readValue("""
                            <Feature>
                                <Attributes>
                                    <Attribute name="src_file">report-2541712c-0684-4f2a-b456-d1cb227c14f6-OfSite-2022-04-05-782478-11-010.xml</Attribute>
                                    <Attribute name="DateUpload">2022-04-05</Attribute>
                                    <Attribute name="cad_num">11:19:0801024:100</Attribute>
                                    <Attribute name="cad_qrtr">11:19:0801024</Attribute>
                                    <Attribute name="area">15</Attribute>
                                    <Attribute name="sk_id">11.У.025</Attribute>
                                    <Attribute name="category">Земли населенных пунктов</Attribute>
                                    <Attribute name="permit_use"/>
                                    <Attribute name="address">Респ. Коми, г. Сосногорск</Attribute>
                                </Attributes>
                                <Geometry>
                                    <Shell>
                                        <Coordinate x="5298714.05" y="842215.52"/>
                                        <Coordinate x="5298719.79" y="842218.35"/>
                                        <Coordinate x="5298720.83" y="842216.25"/>
                                        <Coordinate x="5298715.08" y="842213.43"/>
                                        <Coordinate x="5298714.05" y="842215.52"/>
                                    </Shell>
                                </Geometry>
                            </Feature>
                """, FeatureElement.class);

        SimpleFeature simpleFeature = simpleFeatureFactory.createFeature("test", featureTypeElement, featureElement);
        Object geom_obj = simpleFeature.getProperty("the_geom").getValue();

        assertThat(geom_obj).isNotNull();
        assertThat(geom_obj).isInstanceOf(MultiPolygon.class);

        MultiPolygon multiPolygon = (MultiPolygon) geom_obj;
        Coordinate coordinate = multiPolygon.getCoordinates()[0];

        assertThat(coordinate.getX()).isBetween(49.0, 65.0);
    }
}
