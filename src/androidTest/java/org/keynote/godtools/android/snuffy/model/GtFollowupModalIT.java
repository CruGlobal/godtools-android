package org.keynote.godtools.android.snuffy.model;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.keynote.godtools.android.snuffy.model.GtInputField.Type;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.keynote.godtools.android.snuffy.TestParserUtils.getParserForTestAsset;
import static org.keynote.godtools.android.snuffy.model.Constants.PAGE;

@RunWith(AndroidJUnit4.class)
public class GtFollowupModalIT {
    private static final String TITLE = "Followup Title";
    private static final String BODY = "Followup Body";

    @Test
    public void verifyFollowupModal() throws Exception {
        final GtFollowupModal followup =
                GtFollowupModal.fromXml(PAGE, "simple", getParserForTestAsset("followupmodal-simple.xml"));
        assertNotNull(followup);
        assertThat(followup.getFollowupId(), is(1L));
        assertThat(followup.getTitle(), is(TITLE));
        assertThat(followup.getBody(), is(BODY));

        final List<GtInputField> fields = followup.getInputFields();
        assertThat(fields.size(), is(2));
        final GtInputField field1 = fields.get(0);
        assertThat(field1.getType(), is(Type.EMAIL));
        assertThat(field1.getName(), is("email"));
        final GtInputField field2 = fields.get(1);
        assertThat(field2.getType(), is(Type.TEXT));
        assertThat(field2.getName(), is("name"));
    }
}
