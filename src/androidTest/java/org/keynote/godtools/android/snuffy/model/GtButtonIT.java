package org.keynote.godtools.android.snuffy.model;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.Gravity;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.keynote.godtools.android.event.GodToolsEvent;
import org.keynote.godtools.android.snuffy.model.GtButton.Mode;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.keynote.godtools.android.snuffy.TestParserUtils.getParserForTestAsset;
import static org.keynote.godtools.android.snuffy.TestParserUtils.getRootElementForTestAsset;
import static org.keynote.godtools.android.snuffy.model.Constants.NAMESPACE;
import static org.keynote.godtools.android.snuffy.model.Constants.PAGE;

@RunWith(Parameterized.class)
public class GtButtonIT {
    private static final String TEXT = "Button Text";
    private static final String EXTERNAL_NAMESPACE = "external";
    private static final GodToolsEvent.EventID EVENT1 = new GodToolsEvent.EventID(NAMESPACE, "event1");
    private static final GodToolsEvent.EventID EVENT2 = new GodToolsEvent.EventID(EXTERNAL_NAMESPACE, "event2");

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

    @Test
    public void verifyButtonPositioning() throws Exception {
        final GtButton button = parse("button-default-positioning.xml");
        assertNotNull(button);
        assertThat(button.getTop(), is(5));
        assertThat(button.getLeft(), is(15));
        assertThat(button.getWidth(), is(20));
        assertThat(button.getHeight(), is(10));
        assertThat(button.getTopOffset(), is(-3));
        assertThat(button.getLeftOffset(), is(-2));
        assertThat(button.getRightOffset(), is(17));
    }

    @Test
    public void verifyButtonPositioningNone() throws Exception {
        final GtButton button = parse("button-link-element.xml");
        assertNotNull(button);
        assertThat(button.getTop(), nullValue());
        assertThat(button.getLeft(), nullValue());
        assertThat(button.getWidth(), nullValue());
        assertThat(button.getHeight(), nullValue());
        assertThat(button.getTopOffset(), nullValue());
        assertThat(button.getLeftOffset(), nullValue());
        assertThat(button.getRightOffset(), nullValue());
    }

    @Test
    public void verifyButtonTextStyles() throws Exception {
        final GtButton button = parse("button-link-textstyles.xml");
        assertNotNull(button);
        assertThat(button.getMode(), is(Mode.LINK));
        assertThat(button.getText(), is(TEXT));
        assertThat(button.getTextStyle(), is(Typeface.BOLD_ITALIC));
        assertThat(button.getTextAlign(), is(Gravity.CENTER_HORIZONTAL));
        assertThat(button.getTextSize(), is(42));
        assertNotNull(button.getTextColor());
        assertEquals(18, Color.red(button.getTextColor()));
        assertEquals(52, Color.green(button.getTextColor()));
        assertEquals(86, Color.blue(button.getTextColor()));
        assertEquals(255, Color.alpha(button.getTextColor()));
    }

    @Test
    public void verifyButtonNoTextStyles() throws Exception {
        final GtButton button = parse("button-link-no-textstyles.xml");
        assertNotNull(button);
        assertThat(button.getMode(), is(Mode.LINK));
        assertThat(button.getText(), is(TEXT));
        assertThat(button.getTextStyle(), is(Typeface.NORMAL));
        assertThat(button.getTextAlign(), is(Gravity.NO_GRAVITY));
        assertThat(button.getTextSize(), nullValue());
        assertThat(button.getTextColor(), nullValue());
    }

    @NonNull
    private GtButton parse(@NonNull final String file) throws Exception {
        final GtModel model;
        if (mDomParser) {
            model = GtModel.fromXml(PAGE, getRootElementForTestAsset(file));
        } else {
            model = GtModel.fromXml(PAGE, getParserForTestAsset(file));
        }

        assertNotNull(model);
        assertTrue(model instanceof GtButton);
        return (GtButton) model;
    }
}
