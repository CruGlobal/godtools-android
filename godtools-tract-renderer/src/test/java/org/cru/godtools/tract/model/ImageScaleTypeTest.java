package org.cru.godtools.tract.model;

import org.junit.Test;

import static org.cru.godtools.tract.model.ImageScaleType.BOTTOM;
import static org.cru.godtools.tract.model.ImageScaleType.CENTER;
import static org.cru.godtools.tract.model.ImageScaleType.CENTER_Y;
import static org.cru.godtools.tract.model.ImageScaleType.END;
import static org.cru.godtools.tract.model.ImageScaleType.START;
import static org.cru.godtools.tract.model.ImageScaleType.TOP;
import static org.cru.godtools.tract.model.ImageScaleType.parse;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class ImageScaleTypeTest {
    @Test
    public void verifyParse() throws Exception {
        assertThat(parse("start center", null), allOf(is(START | CENTER_Y), not(CENTER)));
        assertThat(parse("start invalid center", null), is(START | CENTER_Y));
        assertThat(parse("start end", null), allOf(is(END | CENTER_Y), not(START | CENTER_Y)));
    }

    @Test
    public void verifyParseDefaultValue() throws Exception {
        for (final int scaleType : new int[] {CENTER, START, END, TOP, BOTTOM}) {
            assertThat(parse(null, scaleType), is(scaleType));
            assertThat(parse("invalid-type", scaleType), is(scaleType));
        }
    }
}
