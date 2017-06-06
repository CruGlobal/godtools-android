package org.cru.godtools.tract.model;

import org.junit.Test;

import static org.cru.godtools.tract.model.ImageScaleType.BOTTOM;
import static org.cru.godtools.tract.model.ImageScaleType.CENTER;
import static org.cru.godtools.tract.model.ImageScaleType.END;
import static org.cru.godtools.tract.model.ImageScaleType.START;
import static org.cru.godtools.tract.model.ImageScaleType.TOP;
import static org.cru.godtools.tract.model.ImageScaleType.isBottom;
import static org.cru.godtools.tract.model.ImageScaleType.isCenter;
import static org.cru.godtools.tract.model.ImageScaleType.isCenterX;
import static org.cru.godtools.tract.model.ImageScaleType.isCenterY;
import static org.cru.godtools.tract.model.ImageScaleType.isEnd;
import static org.cru.godtools.tract.model.ImageScaleType.isStart;
import static org.cru.godtools.tract.model.ImageScaleType.isTop;
import static org.cru.godtools.tract.model.ImageScaleType.parse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
public class ImageScaleTypeTest {
    @Test
    public void verifyParse() throws Exception {
        int scaleType = parse("start unrecognized center", null);
        assertFalse(isCenterX(scaleType));
        assertTrue(isStart(scaleType));
        assertFalse(isEnd(scaleType));
        assertTrue(isCenterY(scaleType));
        assertFalse(isTop(scaleType));
        assertFalse(isBottom(scaleType));
        assertFalse(isCenter(scaleType));

        scaleType =  parse("center end", null);
        assertFalse(isCenterX(scaleType));
        assertFalse(isStart(scaleType));
        assertTrue(isEnd(scaleType));
        assertTrue(isCenterY(scaleType));
        assertFalse(isTop(scaleType));
        assertFalse(isBottom(scaleType));
        assertFalse(isCenter(scaleType));

        scaleType =  parse("center", null);
        assertTrue(isCenterX(scaleType));
        assertFalse(isStart(scaleType));
        assertFalse(isEnd(scaleType));
        assertTrue(isCenterY(scaleType));
        assertFalse(isTop(scaleType));
        assertFalse(isBottom(scaleType));
        assertTrue(isCenter(scaleType));
    }

    @Test
    public void verifyParseConflictingTypes() throws Exception {
        assertThat(parse("start end", null), nullValue());
        assertThat(parse("start top end", null), nullValue());
        assertThat(parse("bottom top end", null), nullValue());
    }

    @Test
    public void verifyParseDefaultValue() throws Exception {
        for (final int scaleType : new int[] {CENTER, START, END, TOP, BOTTOM}) {
            assertThat(parse(null, scaleType), is(scaleType));
            assertThat(parse("invalid-type", scaleType), is(scaleType));
        }
    }
}
