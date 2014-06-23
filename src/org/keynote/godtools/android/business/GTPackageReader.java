package org.keynote.godtools.android.business;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class GTPackageReader {


    public static List<GTPackage> processMetaResponse(InputStream is) {

        List<GTPackage> packageList = new ArrayList<GTPackage>();

        Document xmlDoc;
        try {
            xmlDoc = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(is);
            xmlDoc.normalize();

            NodeList nlLanguage = xmlDoc.getElementsByTagName("language");
            String language = ((Element) nlLanguage.item(0)).getAttribute("code");

            NodeList nlPackages = xmlDoc.getElementsByTagName("package");
            int numPackages = nlPackages.getLength();

            for (int i = 0; i < numPackages; i++) {
                Element element = (Element) nlPackages.item(i);

                String name = element.getAttribute("name");
                String code = element.getAttribute("code");
                double version = Double.valueOf(element.getAttribute("version"));

                GTPackage gtp = new GTPackage();
                gtp.setCode(code);
                gtp.setName(name);
                gtp.setVersion(version);
                gtp.setLanguage(language);

                packageList.add(gtp);
            }

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        return packageList;
    }

    public static List<GTPackage> processContentFile(File contentFile) {
        List<GTPackage> packageList = new ArrayList<GTPackage>();

        Document xmlDoc;

        try {
            xmlDoc = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(contentFile);
            xmlDoc.normalize();

            NodeList nlResources = xmlDoc.getElementsByTagName("resource");
            int numResources = nlResources.getLength();

            for (int i = 0; i < numResources; i++) {
                Element element = (Element) nlResources.item(i);

                String code = element.getAttribute("package");
                String configFileName = element.getAttribute("config");
                String language = element.getAttribute("language");

                GTPackage gtp = new GTPackage();
                gtp.setCode(code);
                gtp.setConfigFileName(configFileName);
                gtp.setLanguage(language);

                packageList.add(gtp);
            }


        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return packageList;
    }

    public static List<GTPackage> processContentFile(InputStream is) {
        List<GTPackage> packageList = new ArrayList<GTPackage>();

        Document xmlDoc;

        try {
            xmlDoc = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(is);
            xmlDoc.normalize();

            NodeList nlResources = xmlDoc.getElementsByTagName("resource");
            int numResources = nlResources.getLength();

            for (int i = 0; i < numResources; i++) {
                Element element = (Element) nlResources.item(i);

                String code = element.getAttribute("package");
                String configFileName = element.getAttribute("config");
                String language = element.getAttribute("language");

                GTPackage gtp = new GTPackage();
                gtp.setCode(code);
                gtp.setConfigFileName(configFileName);
                gtp.setLanguage(language);

                packageList.add(gtp);
            }


        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return packageList;
    }

}
