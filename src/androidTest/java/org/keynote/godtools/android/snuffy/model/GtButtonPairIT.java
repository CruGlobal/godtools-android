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
public class GtButtonPairIT {
    @Test
    public void verifyButtonPair() throws Exception {
        final GtButtonPair buttonPair = GtButtonPair.fromXml(getParserForTestAsset("buttonpair-simple.xml"));
        assertNotNull(buttonPair);
        assertNotNull(buttonPair.getPositiveButton());
        assertNotNull(buttonPair.getNegativeButton());

        assertThat(buttonPair.getPositiveButton().getMode(), is(Mode.DEFAULT));
        assertThat(buttonPair.getNegativeButton().getMode(), is(Mode.LINK));
    }
}
