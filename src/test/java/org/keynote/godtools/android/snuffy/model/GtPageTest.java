package org.keynote.godtools.android.snuffy.model;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.keynote.godtools.android.snuffy.model.Constants.MANIFEST;

public class GtPageTest {
    @Test
    public void verifyGetUuid() throws Exception {
        // create a page used for testing
        @SuppressWarnings("ConstantConditions")
        final GtPage page = new GtPage(MANIFEST, "test");

        page.mFileName = "e9ff9f3a-2e57-4c7e-b858-2843ada373ae.xml";
        assertThat(page.getUuid(), is("e9ff9f3a-2e57-4c7e-b858-2843ada373ae"));

        page.mFileName = "00000000-0000-0000-0000-000000000000.xml";
        assertThat(page.getUuid(), is("00000000-0000-0000-0000-000000000000"));

        page.mFileName = "other.xml";
        assertThat(page.getUuid(), nullValue());

        page.mFileName = null;
        assertThat(page.getUuid(), nullValue());

        page.mFileName = "00000000-0000-0000-0000-000000000000.txt";
        assertThat(page.getUuid(), nullValue());
    }
}
