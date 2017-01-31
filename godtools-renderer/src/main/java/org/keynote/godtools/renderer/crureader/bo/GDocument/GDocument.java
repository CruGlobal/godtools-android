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
    private String mConfigFileName;
    private String mLanguageCode;
    private String mPackageName;

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

    public String getConfigFileName() {
        return this.mConfigFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.mConfigFileName = configFileName;
    }

    public String getLanguageCode() {
        return this.mLanguageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.mLanguageCode = languageCode;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }
}
