package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.cru.godtools.tract.util.TestParserUtils.getParserForTestAsset;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ManifestIT {
    @Test
    public void verifyParseEmptyManifest() throws Exception {
        final Manifest manifest = parseManifestXml("manifest-empty.xml");
        assertThat(manifest.getPages(), is(empty()));
        assertThat(manifest.mResources, is(empty()));
    }

    @NonNull
    private Manifest parseManifestXml(@NonNull final String file) throws Exception {
        return Manifest.fromXml(getParserForTestAsset(file));
    }
}
