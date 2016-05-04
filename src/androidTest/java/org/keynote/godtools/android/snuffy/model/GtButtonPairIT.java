package org.keynote.godtools.android.snuffy.model;

import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.keynote.godtools.android.snuffy.model.GtButton.Mode;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.keynote.godtools.android.snuffy.TestParserUtils.getParserForTestAsset;
import static org.keynote.godtools.android.snuffy.TestParserUtils.getRootElementForTestAsset;
import static org.keynote.godtools.android.snuffy.model.Constants.PAGE;

@RunWith(Parameterized.class)
public class GtButtonPairIT {
    private final boolean mDomParser;

    @Parameterized.Parameters
    public static ImmutableList<Boolean> data() {
        return ImmutableList.of(true, false);
    }

    public GtButtonPairIT(final boolean domParser) {
        mDomParser = domParser;
    }

    @Test
    public void verifyButtonPair() throws Exception {
        final GtButtonPair buttonPair = parse("buttonpair-simple.xml");
        assertNotNull(buttonPair);
        assertNotNull(buttonPair.getPositiveButton());
        assertNotNull(buttonPair.getNegativeButton());

        assertThat(buttonPair.getPositiveButton().getMode(), is(Mode.DEFAULT));
        assertThat(buttonPair.getNegativeButton().getMode(), is(Mode.LINK));
    }

    @NonNull
    private GtButtonPair parse(@NonNull final String file) throws Exception {
        final GtModel model;
        if (mDomParser) {
            model = GtModel.fromXml(PAGE, getRootElementForTestAsset(file));
        } else {
            model = GtModel.fromXml(PAGE, getParserForTestAsset(file));
        }

        assertNotNull(model);
        assertTrue(model instanceof GtButtonPair);
        return (GtButtonPair) model;
    }

}
