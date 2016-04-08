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
    @Test
    public void verifyPanelButton() throws Exception {
        final GtButton button = GtButton.fromXml(getParserForTestAsset("button-panel1.xml"));
        assertNotNull(button);
        assertThat(button.getMode(), is(Mode.PANEL));
    }
}
