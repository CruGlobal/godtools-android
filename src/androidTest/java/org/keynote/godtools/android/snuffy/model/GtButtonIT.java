package org.keynote.godtools.android.snuffy.model;

import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.keynote.godtools.android.event.GodToolsEvent;
import org.keynote.godtools.android.snuffy.model.GtButton.Mode;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.keynote.godtools.android.snuffy.TestParserUtils.getParserForTestAsset;
import static org.keynote.godtools.android.snuffy.TestParserUtils.getRootElementForTestAsset;

@RunWith(Parameterized.class)
public class GtButtonIT {
    private static final String TEXT = "Button Text";
    private static final String NAMESPACE = "ns";
    private static final GodToolsEvent.EventID EVENT1 = new GodToolsEvent.EventID(NAMESPACE, "event1");
    private static final GodToolsEvent.EventID EVENT2 = new GodToolsEvent.EventID(NAMESPACE, "event2");
    private static final GtPage PAGE = new GtPage(new GtManifest(NAMESPACE));

    private final boolean mDomParser;

    @Parameterized.Parameters
    public static ImmutableList<Boolean> data() {
        return ImmutableList.of(true, false);
    }

    public GtButtonIT(final boolean domParser) {
        mDomParser = domParser;
    }

    @Test
    public void verifyButtonLinkElement() throws Exception {
        final GtButton button = parse("button-link-element.xml");
        assertNotNull(button);
        assertThat(button.getMode(), is(Mode.LINK));
        assertThat(button.getTapEvents(), hasItems(EVENT1, EVENT2));
        assertThat(button.getText(), is(TEXT));
    }

    @Test
    public void verifyButtonLinkMode() throws Exception {
        final GtButton button = parse("button-link-mode.xml");
        assertNotNull(button);
        assertThat(button.getMode(), is(Mode.LINK));
        assertThat(button.getTapEvents(), hasItems(EVENT1, EVENT2));
        assertThat(button.getText(), is(TEXT));
    }

    @Test
    public void verifyButtonPanel() throws Exception {
        final GtButton button = parse("button-panel-simple.xml");
        assertNotNull(button);
        assertThat(button.getMode(), is(Mode.PANEL));
        assertThat(button.getText(), is(TEXT));
    }

    @Test
    public void verifyButtonUnknown() throws Exception {
        final GtButton button = parse("button-unknown-simple.xml");
        assertNotNull(button);
        assertThat(button.getMode(), is(Mode.UNKNOWN));
    }

    @NonNull
    private GtButton parse(@NonNull final String file) throws Exception {
        if (mDomParser) {
            return GtButton.fromXml(PAGE, getRootElementForTestAsset(file));
        } else {
            return GtButton.fromXml(PAGE, getParserForTestAsset(file));
        }
    }
}
