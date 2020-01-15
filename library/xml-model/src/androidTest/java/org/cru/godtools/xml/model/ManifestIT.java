package org.cru.godtools.xml.model;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.cru.godtools.xml.util.TestParserUtils.getParserForTestAsset;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ManifestIT {
    @Test
    public void verifyParseEmptyManifest() throws Exception {
        final Manifest manifest = parseManifestXml("manifest-empty.xml");
        assertThat(manifest.getPages(), is(empty()));
        assertThat(manifest.mResources.size(), is(0));
    }

    @NonNull
    private Manifest parseManifestXml(@NonNull final String file) throws Exception {
        return Manifest.fromXml(getParserForTestAsset(file), file, "kgp", Locale.ENGLISH);
    }
}
