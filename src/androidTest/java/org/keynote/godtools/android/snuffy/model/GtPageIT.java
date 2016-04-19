package org.keynote.godtools.android.snuffy.model;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.keynote.godtools.android.snuffy.TestParserUtils.getParserForTestAsset;
import static org.keynote.godtools.android.snuffy.model.Constants.MANIFEST;

@RunWith(AndroidJUnit4.class)
public class GtPageIT {
    @Test
    public void verifyParsePageXmlSimple() throws Exception {
        final GtPage page = parsePageXml("page-simple.xml");

        assertNull(page.getColor());
        assertNull(page.getWatermark());
    }

    @Test
    public void verifyParsePageXmlFull() throws Exception {
        final GtPage page = parsePageXml("page-full.xml");

        assertNotNull(page.getColor());
        assertEquals(220, Color.red(page.getColor()));
        assertEquals(92, Color.green(page.getColor()));
        assertEquals(49, Color.blue(page.getColor()));
        assertEquals(255, Color.alpha(page.getColor()));
        assertEquals("90458873df4460124f51a61d5e22e2c3d021a739.png", page.getWatermark());
    }

    @NonNull
    private GtPage parsePageXml(@NonNull final String file) throws Exception {
        final GtPage page = new GtPage(MANIFEST);
        page.parsePageXml(getParserForTestAsset(file));
        return page;
    }
}
