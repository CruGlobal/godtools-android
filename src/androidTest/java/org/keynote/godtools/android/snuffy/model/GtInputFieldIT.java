package org.keynote.godtools.android.snuffy.model;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.keynote.godtools.android.snuffy.model.GtInputField.Type;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.keynote.godtools.android.snuffy.TestParserUtils.getParserForTestAsset;
import static org.keynote.godtools.android.snuffy.model.Constants.FOLLOWUP_MODAL;

@RunWith(AndroidJUnit4.class)
public class GtInputFieldIT {
    private static final String NAME = "name";
    private static final String LABEL = "Label";
    private static final String PLACEHOLDER = "Placeholder";
    private static final String[] TEXT_NOTEMPTY = {"abc", " a b c "};
    private static final String[] TEXT_EMPTY = {null, "", "  ", " \t\n\r "};

    @Test
    public void verifyInputFieldEmail() throws Exception {
        final GtInputField field = parse("inputfield-email-simple.xml");
        assertNotNull(field);
        assertThat(field.getType(), is(Type.EMAIL));
        assertThat(field.getName(), is(NAME));
        assertThat(field.getLabel(), is(LABEL));
        assertThat(field.getPlaceholder(), is(PLACEHOLDER));

        assertThat(field.isValidValue(null), is(false));
        assertThat(field.isValidValue("abc"), is(false));
        assertThat(field.isValidValue("test.email@example.com"), is(true));
    }

    @Test
    public void verifyInputFieldText() throws Exception {
        final GtInputField field = parse("inputfield-text-simple.xml");
        assertNotNull(field);
        assertThat(field.getType(), is(Type.TEXT));
        assertThat(field.getName(), is(NAME));
        assertThat(field.getLabel(), is(LABEL));
        assertThat(field.getPlaceholder(), is(PLACEHOLDER));

        for (final String valid : TEXT_NOTEMPTY) {
            assertThat(field.isValidValue(valid), is(true));
        }
        for (final String invalid : TEXT_EMPTY) {
            assertThat(field.isValidValue(invalid), is(true));
        }
    }

    @Test
    public void verifyInputFieldTextValidationNotEmpty() throws Exception {
        final GtInputField field = parse("inputfield-text-pattern-notempty.xml");
        assertNotNull(field);
        assertThat(field.getType(), is(Type.TEXT));

        for (final String valid : TEXT_NOTEMPTY) {
            assertThat(field.isValidValue(valid), is(true));
        }
        for (final String invalid : TEXT_EMPTY) {
            assertThat(field.isValidValue(invalid), is(false));
        }
    }

    @Test
    public void verifyInputFieldUnknown() throws Exception {
        final GtInputField field = parse("inputfield-unknown-simple.xml");
        assertNotNull(field);
        assertThat(field.getType(), is(Type.TEXT));
        assertThat(field.getName(), is(NAME));
        assertThat(field.getLabel(), is(LABEL));
        assertThat(field.getPlaceholder(), is(PLACEHOLDER));
    }

    @NonNull
    private GtInputField parse(@NonNull final String file) throws Exception {
        final GtInputField field = GtInputField.fromXml(FOLLOWUP_MODAL, getParserForTestAsset(file));
        assertNotNull(field);
        return field;
    }
}
