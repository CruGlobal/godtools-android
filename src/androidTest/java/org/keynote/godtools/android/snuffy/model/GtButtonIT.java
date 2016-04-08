package org.keynote.godtools.android.snuffy.model;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.keynote.godtools.android.snuffy.model.GtButton.Mode;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.keynote.godtools.android.snuffy.ParserUtils.getParserForTestAsset;

@RunWith(AndroidJUnit4.class)
public class GtButtonIT {
    private static final String TEXT = "Button Text";

    @Test
    public void verifyButtonLinkElement() throws Exception {
        final GtButton button = GtButton.fromXml(getParserForTestAsset("button-link-element.xml"));
        assertNotNull(button);
        assertThat(button.getMode(), is(Mode.LINK));
        assertThat(button.getText(), is(TEXT));
    }

    @Test
    public void verifyButtonLinkMode() throws Exception {
        final GtButton button = GtButton.fromXml(getParserForTestAsset("button-link-mode.xml"));
        assertNotNull(button);
        assertThat(button.getMode(), is(Mode.LINK));
        assertThat(button.getText(), is(TEXT));
    }

    @Test
    public void verifyButtonPanel() throws Exception {
        final GtButton button = GtButton.fromXml(getParserForTestAsset("button-panel-simple.xml"));
        assertNotNull(button);
        assertThat(button.getMode(), is(Mode.PANEL));
        assertThat(button.getText(), is(TEXT));
    }

    @Test
    public void verifyButtonUnknown() throws Exception {
        final GtButton button = GtButton.fromXml(getParserForTestAsset("button-unknown-simple.xml"));
        assertNotNull(button);
        assertThat(button.getMode(), is(Mode.UNKNOWN));
    }
}
