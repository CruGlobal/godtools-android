package org.keynote.godtools.android.business;

public class GTPackageReader {


//    public static List<GTLanguage> processMetaResponse(InputStream is) {
//
//        List<GTLanguage> languageList = new ArrayList<GTLanguage>();
//
//        Document xmlDoc;
//        try {
//            xmlDoc = DocumentBuilderFactory
//                    .newInstance()
//                    .newDocumentBuilder()
//                    .parse(is);
//            xmlDoc.normalize();
//
//            NodeList nlLanguages = xmlDoc.getElementsByTagName("language");
//            for (int i = 0; i < nlLanguages.getLength(); i++)
//            {
//                Element elLanguage = (Element) nlLanguages.item(i);
//                String languageCode = elLanguage.getAttribute("code");
//                String languageName = elLanguage.getAttribute("name");
//
//                GTLanguage gtl;
//
//                if (languageName == null || languageName.isEmpty())
//                {
//                   gtl = new GTLanguage(languageCode);
//                }
//                else
//                {
//                    gtl = new GTLanguage(languageCode, languageName);
//                }
//
//                List<GTPackage> packageList = new ArrayList<GTPackage>();
//                boolean isDraft = true; // Assume language is draft
//
//                NodeList nlPackages = elLanguage.getElementsByTagName("package");
//                for (int j =0; j < nlPackages.getLength(); j++)
//                {
//                    Element element = (Element) nlPackages.item(j);
//
//                    String code = element.getAttribute("code");
//                    String version = element.getAttribute("version");
//                    String status = element.getAttribute("status");
//
//                    /*
//                     * If any package is live for the language the language will be live. It should
//                     * not be change back to draft by another package.
//                     */
//                    if (isDraft)
//                    {
//                        if ("live".equalsIgnoreCase(status)) isDraft = false;
//                    }
//
//                    GTPackage gtp = new GTPackage();
//                    gtp.setCode(code);
//                    gtp.setVersion(version);
//                    gtp.setLanguage(languageCode);
//                    gtp.setStatus(status);
//
//                    packageList.add(gtp);
//                }
//                gtl.setPackages(packageList);
//                gtl.setDraft(isDraft);
//                languageList.add(gtl);
//            }
//
//        } catch (SAXException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        }
//
//        return languageList;
//    }



//    public static List<GTPackage> processContentFile(File contentFile) {
//        List<GTPackage> packageList = new ArrayList<GTPackage>();
//
//        Document xmlDoc;
//
//        try {
//            xmlDoc = DocumentBuilderFactory
//                    .newInstance()
//                    .newDocumentBuilder()
//                    .parse(contentFile);
//            xmlDoc.normalize();
//
//            NodeList nlResources = xmlDoc.getElementsByTagName("resource");
//            int numResources = nlResources.getLength();
//
//            for (int i = 0; i < numResources; i++) {
//                Element element = (Element) nlResources.item(i);
//
//                String code = element.getAttribute("package");
//                String configFileName = element.getAttribute("config");
//                String language = element.getAttribute("language");
//                String icon = element.getAttribute("icon");
//                String status = element.getAttribute("status");
//                String name = element.getAttribute("name");
//                String version = element.getAttribute("version");
//
//                GTPackage gtp = new GTPackage();
//                gtp.setCode(code);
//                gtp.setConfigFileName(configFileName);
//                gtp.setLanguage(language);
//                gtp.setIcon(icon);
//                gtp.setStatus(status);
//                gtp.setName(name);
//                gtp.setVersion(version);
//
//                packageList.add(gtp);
//            }
//
//
//        } catch (SAXException e) {
//            e.printStackTrace();
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return packageList;
//    }

//    public static List<GTPackage> processContentFile(InputStream is) {
//        List<GTPackage> packageList = new ArrayList<GTPackage>();
//
//        Document xmlDoc;
//
//        try {
//            xmlDoc = DocumentBuilderFactory
//                    .newInstance()
//                    .newDocumentBuilder()
//                    .parse(is);
//            xmlDoc.normalize();
//
//            NodeList nlResources = xmlDoc.getElementsByTagName("resource");
//            int numResources = nlResources.getLength();
//
//            for (int i = 0; i < numResources; i++) {
//                Element element = (Element) nlResources.item(i);
//
//                String code = element.getAttribute("package");
//                String configFileName = element.getAttribute("config");
//                String language = element.getAttribute("language");
//                String icon = element.getAttribute("icon");
//                String status = element.getAttribute("status");
//                String name = element.getAttribute("name");
//                String version = element.getAttribute("version");
//
//                GTPackage gtp = new GTPackage();
//                gtp.setCode(code);
//                gtp.setConfigFileName(configFileName);
//                gtp.setLanguage(language);
//                gtp.setIcon(icon);
//                gtp.setStatus(status);
//                gtp.setName(name);
//                gtp.setVersion(version);
//
//                packageList.add(gtp);
//            }
//
//
//        } catch (SAXException e) {
//            e.printStackTrace();
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return packageList;
//    }
}
