package org.keynote.godtools.android.business;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;

import java.io.InputStream;
import java.util.ArrayList;

@Root(name = "languages")
public class GTLanguages {
    @ElementList(inline = true, required = true, name = "content", entry = "language", type = GTLanguage.class)
    public ArrayList<GTLanguage> mLanguages = new ArrayList<GTLanguage>();

    public static ArrayList<GTLanguage> processContentFile(InputStream is) {

        Registry registry = new Registry();
        Strategy strategy = new RegistryStrategy(registry);
        Serializer serializer = new Persister(strategy);

        try {
            return serializer.read(GTLanguages.class, is).mLanguages;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
