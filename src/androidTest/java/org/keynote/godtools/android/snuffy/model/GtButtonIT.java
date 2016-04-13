package org.keynote.godtools.android.snuffy.model;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keynote.godtools.android.snuffy.model.GtButton.Mode;
import org.keynote.godtools.android.utils.EventID;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.keynote.godtools.android.snuffy.TestParserUtils.getParserForTestAsset;

@RunWith(AndroidJUnit4.class)
public class GtButtonIT {
    private static final String TEXT = "Button Text";
    private static final EventID EVENT1 = new EventID();
    private static final EventID EVENT2 = new EventID();
    private static final String NAMESPACE = "ns";
    private static final GtPage PAGE = new GtPage(new GtManifest(NAMESPACE));

    @Before
    public void before()
    {
        EVENT1.setId("event1");
        EVENT1.setNamespace(NAMESPACE);

        EVENT2.setId("event2");
        EVENT2.setNamespace(NAMESPACE);
    }

    @Test
    public void verifyButtonLinkElement() throws Exception {
        final GtButton button = GtButton.fromXml(PAGE, getParserForTestAsset("button-link-element.xml"));
        assertNotNull(button);
        assertThat(button.getMode(), is(Mode.LINK));
        assertThat(button.getTapEvents(), hasItems(EVENT1, EVENT2));
        assertThat(button.getText(), is(TEXT));
    }

    @Test
    public void verifyButtonLinkMode() throws Exception {
        final GtButton button = GtButton.fromXml(PAGE, getParserForTestAsset("button-link-mode.xml"));
        assertNotNull(button);
        assertThat(button.getMode(), is(Mode.LINK));
        assertThat(button.getTapEvents(), hasItems(EVENT1, EVENT2));
        assertThat(button.getText(), is(TEXT));
    }

    @Test
    public void verifyButtonPanel() throws Exception {
        final GtButton button = GtButton.fromXml(PAGE, getParserForTestAsset("button-panel-simple.xml"));
        assertNotNull(button);
        assertThat(button.getMode(), is(Mode.PANEL));
        assertThat(button.getText(), is(TEXT));
    }

    @Test
    public void verifyButtonUnknown() throws Exception {
        final GtButton button = GtButton.fromXml(PAGE, getParserForTestAsset("button-unknown-simple.xml"));
        assertNotNull(button);
        assertThat(button.getMode(), is(Mode.UNKNOWN));
    }
}
