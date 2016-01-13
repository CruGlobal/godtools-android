package org.keynote.godtools.android.tasks;

import android.util.Log;

import com.google.common.collect.Sets;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DeletedPackageRemovalTask implements Runnable
{
    private final static String TAG = DeletedPackageRemovalTask.class.getSimpleName();

    private final GTLanguage godToolsLanguage;
    private final SnuffyApplication application;
    private final File resourcesDirectory;

    public DeletedPackageRemovalTask(GTLanguage godToolsLanguage,
                                     SnuffyApplication application)
    {
        this.godToolsLanguage = godToolsLanguage;
        this.application = application;
        this.resourcesDirectory = application.getResourcesDir();
    }

    @Override
    public void run()
    {
        DBAdapter dbAdapter = DBAdapter.getInstance(application.getApplicationContext());

        List<GTPackage> godToolsPackages = dbAdapter.getGTPackageByLanguage(godToolsLanguage.getLanguageCode());

        for(GTPackage godToolsPackage : godToolsPackages)
        {
            try
            {
                File configFile = new File(resourcesDirectory,godToolsPackage.getConfigFileName());

                for(String pageFilename : extractPageFilenamesFromConfigXml(new FileInputStream(configFile)))
                {
                    File pageFile = new File(resourcesDirectory + File.separator + pageFilename);
                    if(pageFile.exists())
                    {
                        Log.d(TAG, String.format("Deleting page %s", pageFile.getAbsolutePath()));
                        pageFile.delete();
                    }
                }

                Log.d(TAG, String.format("Deleting config file %s", configFile.getAbsolutePath()));
                configFile.delete();
            }
            catch(FileNotFoundException fileNotFound)
            {
                Log.i(TAG,
                        String.format("Config file for %s-%s not found",
                                godToolsLanguage.getLanguageCode(),
                                godToolsPackage.getCode()),
                        fileNotFound);
            }
            catch(Exception e)
            {
                Log.e(TAG,
                        String.format("Config file for %s-%s not found",
                                godToolsLanguage.getLanguageCode(),
                                godToolsPackage.getCode()),
                        e);
            }
        }

        dbAdapter.deletePackages(godToolsLanguage.getLanguageCode(), "live");
        dbAdapter.deletePackages(godToolsLanguage.getLanguageCode(), "draft");

        godToolsLanguage.setDownloaded(false);
        dbAdapter.updateGTLanguage(godToolsLanguage);
    }

    private Set<String> extractPageFilenamesFromConfigXml(InputStream isMain)
    {
        Set<String> filenameSet = Sets.newHashSet();
        try
        {
            Document xmlDoc = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(isMain);

            Element root = xmlDoc.getDocumentElement();
            if (root == null)
            {
                throw new SAXException("XML Document has no root element");
            }

            NodeList pagesNodeList = root.getElementsByTagName("page");

            for(int i = 0; i < pagesNodeList.getLength(); i++)
            {
                Node pageNode = pagesNodeList.item(i);
                Node filenameAttribute = pageNode.getAttributes().getNamedItem("filename");

                filenameSet.add(filenameAttribute.getNodeValue());

            }

            NodeList aboutNodeList = root.getElementsByTagName("about");

            for(int i = 0; i < aboutNodeList.getLength(); i++)
            {
                Node pageNode = pagesNodeList.item(i);
                Node filenameAttribute = pageNode.getAttributes().getNamedItem("filename");

                filenameSet.add(filenameAttribute.getNodeValue());
            }
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return filenameSet;
    }
}
