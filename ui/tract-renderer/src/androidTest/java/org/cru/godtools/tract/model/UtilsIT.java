package org.cru.godtools.tract.model;

import android.graphics.Color;
import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.cru.godtools.tract.model.Utils.parseColor;
import static org.cru.godtools.tract.model.Utils.parseUrl;
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

    private static final Map<String, Uri> TEST_URLS = ImmutableMap.<String, Uri>builder()
            .put("https://www.example.com/path", Uri.parse("https://www.example.com/path"))
            .put("www.example.com/path", Uri.parse("http://www.example.com/path"))
            .put("mailto:someone@example.com", Uri.parse("mailto:someone@example.com"))
            .build();
    @Test
    public void verifyParseUrl() throws Exception {
        for (final Map.Entry<String, Uri> url : TEST_URLS.entrySet()) {
            assertThat(parseUrl(url.getKey(), null), is(url.getValue()));
        }
    }
}
