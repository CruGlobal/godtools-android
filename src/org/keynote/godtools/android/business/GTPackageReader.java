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


    public static List<GTLanguage> processMetaResponse(InputStream is) {

        List<GTLanguage> languageList = new ArrayList<GTLanguage>();

        Document xmlDoc;
        try {
            xmlDoc = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(is);
            xmlDoc.normalize();

            NodeList nlLanguages = xmlDoc.getElementsByTagName("language");
            for (int i = 0; i < nlLanguages.getLength(); i++) {
                Element elLanguage = (Element) nlLanguages.item(i);
                String languageCode = elLanguage.getAttribute("code");

                GTLanguage gtl = new GTLanguage(languageCode);
                List<GTPackage> packageList = new ArrayList<GTPackage>();

                NodeList nlPackages = elLanguage.getElementsByTagName("package");
                for (int j =0; j < nlPackages.getLength(); j++){
                    Element element = (Element) nlPackages.item(j);

                    String code = element.getAttribute("code");
                    double version = Double.valueOf(element.getAttribute("version"));
                    String status = element.getAttribute("status");

                    GTPackage gtp = new GTPackage();
                    gtp.setCode(code);
                    gtp.setVersion(version);
                    gtp.setLanguage(languageCode);
                    gtp.setStatus(status);

                    packageList.add(gtp);
                }
                gtl.setPackages(packageList);
                languageList.add(gtl);
            }

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        return languageList;
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
                String icon = element.getAttribute("icon");
                String status = element.getAttribute("status");
                String name = element.getAttribute("name");
                double version = Double.parseDouble(element.getAttribute("version"));

                GTPackage gtp = new GTPackage();
                gtp.setCode(code);
                gtp.setConfigFileName(configFileName);
                gtp.setLanguage(language);
                gtp.setIcon(icon);
                gtp.setStatus(status);
                gtp.setName(name);
                gtp.setVersion(version);

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
                String icon = element.getAttribute("icon");
                String status = element.getAttribute("status");
                String name = element.getAttribute("name");
                double version = Double.parseDouble(element.getAttribute("version"));

                GTPackage gtp = new GTPackage();
                gtp.setCode(code);
                gtp.setConfigFileName(configFileName);
                gtp.setLanguage(language);
                gtp.setIcon(icon);
                gtp.setStatus(status);
                gtp.setName(name);
                gtp.setVersion(version);

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
