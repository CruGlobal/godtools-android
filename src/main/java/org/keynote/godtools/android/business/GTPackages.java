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

/**
 * Created by rmatt on 2/7/2017.
 */
@Root(name = "packages")
public class GTPackages {
    @ElementList(inline = true, required = true, name = "content", entry = "resource", type = GTPackage.class)
    public ArrayList<GTPackage> mPackages = new ArrayList<GTPackage>();

    public static ArrayList<GTPackage> processContentFile(InputStream contentFile) {

        Registry registry = new Registry();
        Strategy strategy = new RegistryStrategy(registry);
        Serializer serializer = new Persister(strategy);
        try {
            return serializer.read(GTPackages.class, contentFile).mPackages;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
