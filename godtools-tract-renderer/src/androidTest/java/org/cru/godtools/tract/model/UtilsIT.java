package org.cru.godtools.tract.model;

import android.graphics.Color;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.cru.godtools.tract.model.Utils.parseColor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public final class UtilsIT {
    @Test
    public void verifyParseColor() throws Exception {
        assertThat(parseColor("rgba(255,0,0,1)", null), is(Color.RED));
        assertThat(parseColor("rgba(0,255,0,1)", null), is(Color.GREEN));
        assertThat(parseColor("rgba(0,0,255,1)", null), is(Color.BLUE));
        assertThat(parseColor("rgba(0,0,0,1)", null), is(Color.BLACK));

        // default parse behavior
        assertThat(parseColor(null, Color.RED), is(Color.RED));
        assertThat(parseColor("akjsdf", Color.RED), is(Color.RED));
    }
}
