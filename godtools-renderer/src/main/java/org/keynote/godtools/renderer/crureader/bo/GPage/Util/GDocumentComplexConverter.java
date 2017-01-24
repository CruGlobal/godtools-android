package org.keynote.godtools.renderer.crureader.bo.GPage.Util;

import org.keynote.godtools.renderer.crureader.bo.GDocument.GDocument;
import org.keynote.godtools.renderer.crureader.bo.GDocument.GDocumentPage;
import org.keynote.godtools.renderer.crureader.bo.GPage.Event.GodToolsEvent;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderConstants;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import java.util.ArrayList;
import java.util.List;

public class GDocumentComplexConverter implements Converter<GDocument> {
    private static final String TAG = "GDocumentPageCC";

    @Override
    public GDocument read(InputNode node) throws Exception {
        Serializer serializer = new Persister();

        GDocument gDocument = serializer.read(GDocument.class, node);
        List<GDocumentPage> documentPages = gDocument.documentPages;
        for (GDocumentPage gDocumentPage : documentPages) {
            if (gDocumentPage.listeners != null && !gDocumentPage.listeners.equalsIgnoreCase("")) {
                String[] splitListener = RenderConstants.splitEvents(gDocumentPage.listeners);
                if (splitListener != null && splitListener.length > 0) {
                    gDocumentPage.eventListeners = new ArrayList<>();
                    for (String eventId : splitListener) {
                        gDocumentPage.eventListeners.add(new GodToolsEvent.EventID(gDocument.packagename.content, eventId));
                    }
                }
            }
        }
        return gDocument;
    }

    @Override
    public void write(OutputNode node, GDocument value) throws Exception {
        //This library doesn't write any XML
    }
}
