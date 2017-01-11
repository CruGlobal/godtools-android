package org.keynote.godtools.renderer.crureader.bo.GPage.Util;

import org.keynote.godtools.renderer.crureader.bo.GPage.GButton;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

public class GButtonComplexConverter implements Converter<GButton> {
    @Override
    public GButton read(InputNode node) throws Exception {
        Serializer serializer = new Persister();
        String gButtonText = node.getValue();
        GButton gButton = serializer.read(GButton.class, node);
        gButton.setText(gButtonText);
        return gButton;

    }

    @Override
    public void write(OutputNode node, GButton value) throws Exception {
        //This library doesn't write any XML
    }
}
