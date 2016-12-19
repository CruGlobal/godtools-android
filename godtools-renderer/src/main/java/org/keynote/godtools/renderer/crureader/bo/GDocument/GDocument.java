package org.keynote.godtools.renderer.crureader.bo.GDocument;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by rmatt on 10/24/2016.
 * <?xml version="1.0" encoding="UTF-8"?><document lang="en">
 * <packagename gtapi-trx-id="45e65db9-957f-4ca5-b4ca-b60c4ce424c1" translate="true">Knowing God Personally</packagename>
 * <page filename="3a0fe8aa-86bf-4ab5-82b3-4861ae71794b.xml" gtapi-trx-id="33a0a915-a7ab-449f-aef4-f865e1298acb" thumb="PageThumb_01.png" translate="true">Home</page>
 * <page filename="398845c7-fc8d-4ca7-a6af-af00209e5f23.xml" gtapi-trx-id="74b6fb6f-58ec-493e-84f3-4c9278245591" thumb="PageThumb_02.png" translate="true">1 God Loves You And Created You To Know Him Personally.</page>
 * <page filename="e09c6ab5-91de-4a0c-9346-6399b4020da3.xml" gtapi-trx-id="c4378371-9a50-41ee-b4a5-c25bf2320674" thumb="PageThumb_03.png" translate="true">2 We Are Separated From God By Our Sin, So We Cannot Know Him Or Experience His Love.</page>
 * <about filename="6c31dc4c-1cc8-47e1-aa34-1c86049af426.xml" gtapi-trx-id="0c41ea49-9905-46f0-bfa9-400d3807545c" translate="true">About</about>
 * <instructions gtapi-trx-id="cc494223-8e9e-445e-aaa9-0982be68b65e" translate="true"/>
 * </document>
 */
@Root(name = "document")
public class GDocument {
    @Element(required = false)
    public GInstructions instructions;

    @ElementList(inline = true, entry = "page")
    public List<GDocumentPage> pages;

    @Element
    public GPackageName packagename;

    @Element
    public GAbout about;

    @Attribute
    public String lang;
}
