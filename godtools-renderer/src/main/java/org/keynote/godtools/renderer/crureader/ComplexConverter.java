package org.keynote.godtools.renderer.crureader;

import org.keynote.godtools.renderer.crureader.bo.GPage.GButton;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

/**
 * Created by rmatt on 12/21/2016.
 */

/*
This class is meant to fix the issue were there can't be an element containing an element and the same element containing text in simpleXML.  Note the bottom button has text, not elements.

<?xml version="1.0" encoding="UTF-8"?><page buttons="6" color="#DC5C31" tnt-trx-ref-value="" tnt-trx-translated="" translate="" watermark="90458873df4460124f51a61d5e22e2c3d021a739.png">
  <title h="50" mode="straight">
    <heading gtapi-trx-id="99627019-e11a-4678-bc23-0cfcd5a6ed62" textalign="center" translate="true">WEBSITES TO ASSIST YOU</heading>
  </title>
  <button>
    <buttontext gtapi-trx-id="2b883c27-3cf3-4046-a075-f5ddd97c881e" translate="true">Still not sure who Jesus is?</buttontext>
    <panel>
      <button gtapi-trx-id="3285f0ab-f247-4524-bcc1-b757d2db85a9" mode="url" tnt-trx-ref-value="whyjesus.com.au" tnt-trx-translated="true" translate="true">www.everystudent.com</button>
    </panel>
  </button>
  <button>
    <buttontext gtapi-trx-id="22ddc59d-91cc-4432-90a9-1730d995f7d3" translate="true">More about Christianity...</buttontext>
    <panel>
      <button gtapi-trx-id="f1225cff-2834-486a-a93e-b8dd4efd7a5c" mode="url" translate="true">www.startingwithgod.com</button>
    </panel>
  </button>
  <button>
    <buttontext gtapi-trx-id="0a54d46a-93af-4f0d-804f-4bd48ce5e385" translate="true">Watch a film about Jesus</buttontext>
    <panel>
      <button gtapi-trx-id="1c0b32ac-31ba-4606-9de8-05034082e178" mode="url" tnt-trx-ref-value="www.5clicks.com" tnt-trx-translated="true" translate="true">jesusfilmmedia.org/video/1_529-jf-0-0/english/jesus</button>
    </panel>
  </button>
  <button gtapi-trx-id="55654320-db11-4cb3-b1ea-3aea73bb5079" mode="allurl" translate="true">All Websites</button>
</page>
 */
public class ComplexConverter implements Converter<GButton> {
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
