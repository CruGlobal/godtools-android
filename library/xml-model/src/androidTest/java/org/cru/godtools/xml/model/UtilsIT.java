package org.cru.godtools.xml.model;

import android.graphics.Color;
import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.cru.godtools.xml.model.Utils.parseColor;
import static org.cru.godtools.xml.model.Utils.parseUrl;
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

    @Test
    public void verifyParseUrl() throws Exception {
        assertThat(parseUrl("https://www.example.com/path", null), is(Uri.parse("https://www.example.com/path")));
        assertThat(parseUrl("www.example.com/path", null), is(Uri.parse("http://www.example.com/path")));
        assertThat(parseUrl("mailto:someone@example.com", null), is(Uri.parse("mailto:someone@example.com")));
    }
}
