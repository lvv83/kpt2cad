package ru.agiks.kptservice.xml;


import ru.agiks.kptservice.xml.model.FeatureElement;
import ru.agiks.kptservice.xml.model.FeatureTypeElement;

public interface XmlFeatureListener {
    void onFeatureType(FeatureTypeElement featureType);

    void onFeature(FeatureElement feature);

    void onShapeFilePrefix(String prefix);
}
