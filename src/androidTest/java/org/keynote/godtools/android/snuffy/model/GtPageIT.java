package org.keynote.godtools.android.snuffy.model;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.keynote.godtools.android.snuffy.TestParserUtils.getParserForTestAsset;
import static org.keynote.godtools.android.snuffy.model.Constants.MANIFEST;

@RunWith(AndroidJUnit4.class)
public class GtPageIT {
    @Test
    public void verifyParsePageXmlSimple() throws Exception {
        final GtPage page = parsePageXml("page-simple.xml");

        assertNull(page.getBackgroundColor());
        assertNull(page.getWatermark());
    }

    @Test
    public void verifyParsePageXmlFull() throws Exception {
        final GtPage page = parsePageXml("page-full.xml");

        assertNotNull(page.getBackgroundColor());
        assertEquals(220, Color.red(page.getBackgroundColor()));
        assertEquals(92, Color.green(page.getBackgroundColor()));
        assertEquals(49, Color.blue(page.getBackgroundColor()));
        assertEquals(255, Color.alpha(page.getBackgroundColor()));
        assertEquals("90458873df4460124f51a61d5e22e2c3d021a739.png", page.getWatermark());
    }

    @Test
    public void verifyParsePageXmlBackgrounds() throws Exception {
        final GtPage page = parsePageXml("page-background.xml");

        assertNotNull(page.getBackgroundColor());
        assertEquals(18, Color.red(page.getBackgroundColor()));
        assertEquals(52, Color.green(page.getBackgroundColor()));
        assertEquals(86, Color.blue(page.getBackgroundColor()));
        assertEquals(255, Color.alpha(page.getBackgroundColor()));
        assertEquals("background.jpg", page.getBackground());
        assertEquals("watermark.png", page.getWatermark());
        assertTrue(page.hasPageShadows());

        // page color should affect button color
        final GtFollowupModal modal = page.getFollowupModal("kgp-test-followup-1");
        assertNotNull(modal);
        assertNotNull(modal.getButtonPair());
        final GtButton button = modal.getButtonPair().getPositiveButton();
        assertNotNull(button);
        assertNotNull(button.getTextColor());
        assertEquals(18, Color.red(button.getTextColor()));
        assertEquals(52, Color.green(button.getTextColor()));
        assertEquals(86, Color.blue(button.getTextColor()));
        assertEquals(255, Color.alpha(button.getTextColor()));
    }

    @Test
    public void verifyParsePageXmlNoBackgrounds() throws Exception {
        final GtPage page = parsePageXml("page-nobackground.xml");

        assertNull(page.getBackgroundColor());
        assertNull(page.getBackground());
        assertNull(page.getWatermark());
        assertFalse(page.hasPageShadows());
    }

    @Test
    public void verifyParsePageXmlShadows() throws Exception {
        final GtPage page = parsePageXml("page-shadows.xml");

        assertTrue(page.hasPageShadows());
    }

    @NonNull
    private GtPage parsePageXml(@NonNull final String file) throws Exception {
        final GtPage page = new GtPage(MANIFEST, "test");
        page.parsePageXml(getParserForTestAsset(file));
        return page;
    }
}
