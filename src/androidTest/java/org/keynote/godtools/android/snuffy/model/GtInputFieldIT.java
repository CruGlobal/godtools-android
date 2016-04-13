package org.keynote.godtools.android.snuffy.model;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.keynote.godtools.android.snuffy.model.GtInputField.Type;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.keynote.godtools.android.snuffy.TestParserUtils.getParserForTestAsset;

@RunWith(AndroidJUnit4.class)
public class GtInputFieldIT {
    private static final String NAME = "name";
    private static final String LABEL = "Label";
    private static final String PLACEHOLDER = "Placeholder";

    @Test
    public void verifyInputFieldEmail() throws Exception {
        final GtInputField field = GtInputField.fromXml(getParserForTestAsset("inputfield-email-simple.xml"));
        assertNotNull(field);
        assertThat(field.getType(), is(Type.EMAIL));
        assertThat(field.getName(), is(NAME));
        assertThat(field.getLabel(), is(LABEL));
        assertThat(field.getPlaceholder(), is(PLACEHOLDER));
    }

    @Test
    public void verifyInputFieldText() throws Exception {
        final GtInputField field = GtInputField.fromXml(getParserForTestAsset("inputfield-text-simple.xml"));
        assertNotNull(field);
        assertThat(field.getType(), is(Type.TEXT));
        assertThat(field.getName(), is(NAME));
        assertThat(field.getLabel(), is(LABEL));
        assertThat(field.getPlaceholder(), is(PLACEHOLDER));
    }

    @Test
    public void verifyInputFieldUnknown() throws Exception {
        final GtInputField field = GtInputField.fromXml(getParserForTestAsset("inputfield-unknown-simple.xml"));
        assertNotNull(field);
        assertThat(field.getType(), is(Type.TEXT));
        assertThat(field.getName(), is(NAME));
        assertThat(field.getLabel(), is(LABEL));
        assertThat(field.getPlaceholder(), is(PLACEHOLDER));
    }
}
