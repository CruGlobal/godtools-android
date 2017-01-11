package org.keynote.godtools.renderer.crureader;

import org.keynote.godtools.renderer.crureader.bo.GDocument.GDocument;
import org.keynote.godtools.renderer.crureader.bo.GPage.GButton;
import org.keynote.godtools.renderer.crureader.bo.GPage.GPage;
import org.keynote.godtools.renderer.crureader.bo.GPage.Util.GButtonComplexConverter;
import org.keynote.godtools.renderer.crureader.bo.GPage.Util.GDocumentComplexConverter;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;

import java.io.File;

/**
 * Created by rmatt on 10/24/2016.
 */

public class XMLUtil {

    public static GPage parseGPage(File file) throws Exception {

        Registry registry = new Registry();
        Strategy strategy = new RegistryStrategy(registry);
        Serializer serializer = new Persister(strategy);
        registry.bind(GButton.class, GButtonComplexConverter.class);
        return serializer.read(GPage.class, file);
    }

    public static GDocument parseGDocument(File file) throws Exception {
        Registry registry = new Registry();
        Strategy strategy = new RegistryStrategy(registry);
        Serializer serializer = new Persister(strategy);
        registry.bind(GDocument.class, GDocumentComplexConverter.class);
        return serializer.read(GDocument.class, file);
    }

}
