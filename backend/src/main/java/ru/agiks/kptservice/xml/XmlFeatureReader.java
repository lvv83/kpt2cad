package ru.agiks.kptservice.xml;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Getter;
import lombok.Setter;
import ru.agiks.kptservice.xml.model.FeatureElement;
import ru.agiks.kptservice.xml.model.FeatureTypeElement;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

// Читает XML-документы в потоковом режиме
public class XmlFeatureReader {
    private final XMLInputFactory xmlInputFactory;
    private final File xmlFile;

    @Getter
    @Setter
    private XmlFeatureListener listener;

    @Getter
    @Setter
    private XmlMapper mapper = new XmlMapper();

    public  XmlFeatureReader(XMLInputFactory xmlInputFactory, File xmlFile)
    {
        this.xmlInputFactory = xmlInputFactory;
        this.xmlFile = xmlFile;
    }

    public void read()
    {
        try (FileInputStream inputStream = new FileInputStream(xmlFile))
        {
            // Префикс шейпфайла планируем использовать как имя для типа данных.
            // В то же время мы не считываем ShapeFileElement посредством XmlMapper, так как
            // это приведёт к буферизации всех данных внутри элемента и потоковой обработки по сути не будет

            XMLStreamReader reader = this.xmlInputFactory.createXMLStreamReader(inputStream);
            while (reader.hasNext())
            {
                reader.next();
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    if ("ShapeFile".equals(reader.getLocalName())) {
                        String prefix = reader.getAttributeValue(null, "prefix");
                        if (prefix != null)
                        {
                            notifyPrefix(prefix);
                        }

                    } else if ("FeatureType".equals(reader.getLocalName())) {
                        FeatureTypeElement featureType = mapper.readValue(reader, FeatureTypeElement.class);
                        notify(featureType);
                    } else if ("Feature".equals(reader.getLocalName())) {
                        FeatureElement feature = mapper.readValue(reader, FeatureElement.class);
                        notify(feature);
                    }
                }
            }
        }
        catch (IOException | XMLStreamException e)
        {
            throw new RuntimeException(e);
        }

    }

    private void notify(FeatureTypeElement featureType)
    {
        if (listener != null)
        {
            listener.onFeatureType(featureType);
        }
    }

    private void notify(FeatureElement feature)
    {
        if (listener != null)
        {
            listener.onFeature(feature);
        }
    }

    private void notifyPrefix(String prefix)
    {
        if (listener != null)
        {
            listener.onShapeFilePrefix(prefix);
        }
    }
}
