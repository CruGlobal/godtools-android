package org.cru.godtools.everystudent;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class EveryStudentHandler extends DefaultHandler
{

    private final List<Map<String, String>> categoryData = new ArrayList<Map<String, String>>();
    private final List<List<Map<String, String>>> topicsData = new ArrayList<List<Map<String, String>>>();
    private final String xmlCategory = "category";
    private final String xmlTopic = "item";
    private String currentElement = null;
    private Map<String, String> curCatMap = null;
    private List<Map<String, String>> curTopics = null;
    private Map<String, String> curTopicMap = null;
    private StringBuffer curContent = null;

    @Override
    public void startElement(String uri, String name, String qName, Attributes atts)
    {
        if (name.equalsIgnoreCase(xmlCategory))
        {
            currentElement = xmlCategory;
            curCatMap = new HashMap<>();
            curCatMap.put(Constants.NAME, atts.getValue("name"));
            curTopics = new ArrayList<>();
        }
        else if (name.equalsIgnoreCase(xmlTopic))
        {
            currentElement = xmlTopic;
            curTopicMap = new HashMap<>();
            curTopicMap.put(Constants.NAME, atts.getValue("name"));
            curContent = new StringBuffer();
        }
    }

    @Override
    public void endElement(String uri, String name, String qName)
    {
        if (name.equalsIgnoreCase(xmlCategory))
        {
            categoryData.add(curCatMap);
            topicsData.add(curTopics);
        }
        if (name.equalsIgnoreCase(xmlTopic))
        {
            curTopicMap.put(Constants.CONTENT, curContent.toString());
            curTopics.add(curTopicMap);
        }
    }

    @Override
    public void characters(char ch[], int start, int length)
    {
        String value = new String(ch, start, length);
        if (currentElement != null && currentElement.equalsIgnoreCase(xmlTopic))
        {
            curContent.append(value);
        }
    }

    public List<Map<String, String>> getCategories()
    {
        return categoryData;
    }


    public List<List<Map<String, String>>> getTopics()
    {
        return topicsData;
    }
}