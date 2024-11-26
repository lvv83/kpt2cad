package ru.agiks.kptservice.xml;


import lombok.Getter;
import lombok.Setter;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.locationtech.jts.geom.*;
import org.springframework.util.StringUtils;
import ru.agiks.kptservice.xml.model.*;
import ru.rkomi.kpt2cad.KnownTransformation;
import ru.rkomi.kpt2cad.TransformationEngine;
import ru.rkomi.kpt2cad.xslt.CoordinateSystemDictionary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Преобразует объекты пакета xml.model в объекты GeoTools SimpleFeature
 */
@Setter
@Getter
public class SimpleFeatureFactory
{
    private boolean useSRID = false; // gt-epsg-hsql должен быть добавлен в зависимости проекта при useSRID = true

    private int SRID = 4326;

    private String geometryPropertyName = "the_geom";

    private String cadastralQuarterPropertyName = "cad_qrtr";

    private final GeometryFactory GEOMETRY_FACTORY;
    private final TransformationEngine TRANSFORMATION_ENGINE;
    private final CoordinateSystemDictionary COORDINATE_SYSTEM_DICTIONARY;

    private final static String POLYGON_GEOMETRY_TYPE = "Polygon";
    private final static String MULTIPOLYGON_GEOMETRY_TYPE = "MultiPolygon";

    private static final Set<String> ALLOWED_GEOMETRY_TYPES = Set.of(
            POLYGON_GEOMETRY_TYPE, MULTIPOLYGON_GEOMETRY_TYPE
    );

    public SimpleFeatureFactory(GeometryFactory geometryFactory, TransformationEngine transformationEngine, CoordinateSystemDictionary coordinateSystemDictionary) {
        GEOMETRY_FACTORY = geometryFactory;
        TRANSFORMATION_ENGINE = transformationEngine;
        COORDINATE_SYSTEM_DICTIONARY = coordinateSystemDictionary;
    }

    public SimpleFeature createFeature(String featureTypeName, FeatureTypeElement featureTypeElement, FeatureElement featureElement)
    {
        SimpleFeatureBuilder featureBuilder = getFeatureBuilder(featureTypeName, featureTypeElement);

        // Создаём JTS-геометрию и добавляем её как первый атрибут

        if (MULTIPOLYGON_GEOMETRY_TYPE.equals(featureTypeElement.getGeometryType())) {
            MultiPolygon multiPolygon = createMultiPolygon(featureElement.getGeometry());

            // Необходимо предварительно выполнить трансформацию
            Geometry transformed = transform(multiPolygon, featureElement);

            featureBuilder.add(transformed);
        }
        else
        {
            // Возможно трактовать Polygon как MultiPolygon, но мы создаём истинный простой полигон
            // Берётся первый shellElement
            Polygon polygon = createPolygon(featureElement.getGeometry());

            // Необходимо предварительно выполнить трансформацию
            Geometry transformed = transform(polygon, featureElement);

            featureBuilder.add(transformed);
        }

        for (AttributeElement attr: featureTypeElement.getAttributes())
        {
            var opt = featureElement.getAttributeByName(attr.getName());
            if (opt.isPresent())
            {
                featureBuilder.add(opt.get().toObjectValue(attr.toSchemaType()));
            }
            else
            {
                featureBuilder.add(null);
            }
        }

        return featureBuilder.buildFeature(null);
    }

    private Map<String, SimpleFeatureBuilder> builders = new HashMap<>();

    private synchronized SimpleFeatureBuilder getFeatureBuilder(String featureTypeName, FeatureTypeElement featureTypeElement)
    {
        if (builders.containsKey(featureTypeName))
            return builders.get(featureTypeName);

        try {
            String typeSpec = this.buildTypeSpec(featureTypeElement);
            SimpleFeatureType featureType = DataUtilities.createType(featureTypeName, typeSpec);
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
            builders.put(featureTypeName, featureBuilder);

            return featureBuilder;

        } catch (SchemaException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildTypeSpec(FeatureTypeElement source)
    {
        if (!StringUtils.hasText(geometryPropertyName))
            throw new RuntimeException("geometryPropertyName not set");

        if (!StringUtils.hasText(source.getGeometryType()))
            throw new RuntimeException("geometryType not set");

        if (source.getAttributes().isEmpty())
            throw new RuntimeException("Empty attributes for featureType");

        // TODO: проверить geometryPropertyName на корректность

        if (!ALLOWED_GEOMETRY_TYPES.contains(source.getGeometryType()))
            throw new RuntimeException(String.format("%s is not supported geometry type", source.getGeometryType()));


        StringBuilder sb = new StringBuilder();

        // Описание геометрии "<ИМЯ_ПОЛЯ_ГЕОМЕТРИИ>:<ТИП_ГЕОМЕТРИИ>[:srid=<ИДЕНТИФИКАТОР_СИСТЕМЫ_КООРДИНАТ>]"
        sb.append(geometryPropertyName);
        sb.append(":");
        sb.append(source.getGeometryType());

        if (useSRID)
        {
            sb.append(String.format(":srid=%d", SRID));
        }

        // Собираем атрибуты через запятую в формате "<ИМЯ_ПОЛЯ>:<ТИП_ПОЛЯ>"

        for (AttributeElement attr: source.getAttributes())
        {
            String definition = String.format("%s:%s", attr.getName(), attr.toSchemaType());
            sb.append(",");
            sb.append(definition);
        }

        return sb.toString();
    }

    private MultiPolygon createMultiPolygon(GeometryElement geometryElement)
    {
        int shellSize = geometryElement.getShells().size();

        Polygon[] polygons = new Polygon[shellSize];

        for (int i = 0; i < shellSize; i++)
        {
            ShellElement shellElement = geometryElement.getShells().get(i);
            polygons[i] = createPolygon(shellElement);
        }

        return GEOMETRY_FACTORY.createMultiPolygon(polygons);
    }

    private Polygon createPolygon(ShellElement shellElement)
    {
        LinearRing shell = createLinearRing(shellElement.getCoordinates());
        int holesSize = shellElement.getHoles().size();

        if (holesSize == 0)
            return GEOMETRY_FACTORY.createPolygon(shell);

        LinearRing[] holes = new LinearRing[holesSize];

        for (int i = 0; i < holesSize; i++)
        {
            HoleElement holeElement = shellElement.getHoles().get(i);
            holes[i] = createLinearRing(holeElement.getCoordinates());
        }

        return GEOMETRY_FACTORY.createPolygon(shell, holes);
    }

    private Polygon createPolygon(GeometryElement geometryElement)
    {
        int shellSize = geometryElement.getShells().size();
        if (shellSize == 0)
            return GEOMETRY_FACTORY.createPolygon();

        ShellElement shellElement = geometryElement.getShells().get(0);
        return createPolygon(shellElement);
    }

    private LinearRing createLinearRing(List<CoordinateElement> coordinates)
    {
        int size = coordinates.size();
        Coordinate[] jts_coordinates = new Coordinate[size];

        for (int i = 0; i < size; i++)
        {
            CoordinateElement el = coordinates.get(i);
            jts_coordinates[i] = new Coordinate(el.getX(), el.getY());
        }

        return GEOMETRY_FACTORY.createLinearRing(jts_coordinates);
    }

    private Geometry transform(Geometry geometry, FeatureElement featureElement)
    {
        var opt = featureElement.getAttributeByName(cadastralQuarterPropertyName);
        if (opt.isPresent())
        {
            String cadastralQuarter = opt.get().getValue();
            KnownTransformation transformation = COORDINATE_SYSTEM_DICTIONARY.getTransformationByCadastralQuarter(cadastralQuarter);

            if (transformation != null)
                return TRANSFORMATION_ENGINE.transform(transformation, geometry);
        }

        return geometry;
    }
}
