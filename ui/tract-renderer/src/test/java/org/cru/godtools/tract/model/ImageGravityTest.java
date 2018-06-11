package org.cru.godtools.tract.model;

import org.junit.Test;

import static org.cru.godtools.tract.model.ImageGravity.BOTTOM;
import static org.cru.godtools.tract.model.ImageGravity.CENTER;
import static org.cru.godtools.tract.model.ImageGravity.END;
import static org.cru.godtools.tract.model.ImageGravity.START;
import static org.cru.godtools.tract.model.ImageGravity.TOP;
import static org.cru.godtools.tract.model.ImageGravity.isBottom;
import static org.cru.godtools.tract.model.ImageGravity.isCenter;
import static org.cru.godtools.tract.model.ImageGravity.isCenterX;
import static org.cru.godtools.tract.model.ImageGravity.isCenterY;
import static org.cru.godtools.tract.model.ImageGravity.isEnd;
import static org.cru.godtools.tract.model.ImageGravity.isStart;
import static org.cru.godtools.tract.model.ImageGravity.isTop;
import static org.cru.godtools.tract.model.ImageGravity.parse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
public class ImageGravityTest {
    @Test
    public void verifyParse() throws Exception {
        int gravity = parse("start unrecognized center", null);
        assertFalse(isCenterX(gravity));
        assertTrue(isStart(gravity));
        assertFalse(isEnd(gravity));
        assertTrue(isCenterY(gravity));
        assertFalse(isTop(gravity));
        assertFalse(isBottom(gravity));
        assertFalse(isCenter(gravity));

        gravity = parse("center end", null);
        assertFalse(isCenterX(gravity));
        assertFalse(isStart(gravity));
        assertTrue(isEnd(gravity));
        assertTrue(isCenterY(gravity));
        assertFalse(isTop(gravity));
        assertFalse(isBottom(gravity));
        assertFalse(isCenter(gravity));

        gravity = parse("center", null);
        assertTrue(isCenterX(gravity));
        assertFalse(isStart(gravity));
        assertFalse(isEnd(gravity));
        assertTrue(isCenterY(gravity));
        assertFalse(isTop(gravity));
        assertFalse(isBottom(gravity));
        assertTrue(isCenter(gravity));
    }

    @Test
    public void verifyParseConflictingGravity() throws Exception {
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
