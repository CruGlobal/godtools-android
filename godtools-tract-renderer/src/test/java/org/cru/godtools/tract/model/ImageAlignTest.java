package org.cru.godtools.tract.model;

import org.junit.Test;

import static org.cru.godtools.tract.model.ImageAlign.BOTTOM;
import static org.cru.godtools.tract.model.ImageAlign.CENTER;
import static org.cru.godtools.tract.model.ImageAlign.END;
import static org.cru.godtools.tract.model.ImageAlign.START;
import static org.cru.godtools.tract.model.ImageAlign.TOP;
import static org.cru.godtools.tract.model.ImageAlign.isBottom;
import static org.cru.godtools.tract.model.ImageAlign.isCenter;
import static org.cru.godtools.tract.model.ImageAlign.isCenterX;
import static org.cru.godtools.tract.model.ImageAlign.isCenterY;
import static org.cru.godtools.tract.model.ImageAlign.isEnd;
import static org.cru.godtools.tract.model.ImageAlign.isStart;
import static org.cru.godtools.tract.model.ImageAlign.isTop;
import static org.cru.godtools.tract.model.ImageAlign.parse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
public class ImageAlignTest {
    @Test
    public void verifyParse() throws Exception {
        int align = parse("start unrecognized center", null);
        assertFalse(isCenterX(align));
        assertTrue(isStart(align));
        assertFalse(isEnd(align));
        assertTrue(isCenterY(align));
        assertFalse(isTop(align));
        assertFalse(isBottom(align));
        assertFalse(isCenter(align));

        align = parse("center end", null);
        assertFalse(isCenterX(align));
        assertFalse(isStart(align));
        assertTrue(isEnd(align));
        assertTrue(isCenterY(align));
        assertFalse(isTop(align));
        assertFalse(isBottom(align));
        assertFalse(isCenter(align));

        align = parse("center", null);
        assertTrue(isCenterX(align));
        assertFalse(isStart(align));
        assertFalse(isEnd(align));
        assertTrue(isCenterY(align));
        assertFalse(isTop(align));
        assertFalse(isBottom(align));
        assertTrue(isCenter(align));
    }

    @Test
    public void verifyParseConflictingAlignment() throws Exception {
        assertThat(parse("start end", null), nullValue());
        assertThat(parse("start top end", null), nullValue());
        assertThat(parse("bottom top end", null), nullValue());
    }

    @Test
    public void verifyParseDefaultValue() throws Exception {
        for (final int align : new int[] {CENTER, START, END, TOP, BOTTOM}) {
            assertThat(parse(null, align), is(align));
            assertThat(parse("invalid-type", align), is(align));
        }
    }
}
