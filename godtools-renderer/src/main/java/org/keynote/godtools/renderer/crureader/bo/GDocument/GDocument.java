package org.keynote.godtools.renderer.crureader.bo.GDocument;

import android.util.Log;

import org.keynote.godtools.renderer.crureader.bo.GPage.Event.GodToolsEvent;
import org.keynote.godtools.renderer.crureader.bo.GPage.GPage;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "document")
public class GDocument {

    private static final String TAG = "GDocument";
    @Element(required = false)
    public GInstructions instructions;

    @ElementList(inline = true, entry = "page")
    public List<GDocumentPage> documentPages;

    @Element
    public GPackageName packagename;

    @Element
    public GAbout about;

    @Attribute
    public String lang;

    private List<GPage> pages;

    public List<GPage> getPages() {
        return pages;
    }

    public int getGlobalEventPosition(GodToolsEvent.EventID event) {
        for (int i = 0; i < documentPages.size(); i++) {

            if (documentPages.get(i).eventListeners != null && documentPages.get(i).eventListeners.contains(event)) {
                Log.i(TAG, "event found!");
                return i;
            }
        }
        return GodToolsEvent.INVALID_ID;
    }

}
