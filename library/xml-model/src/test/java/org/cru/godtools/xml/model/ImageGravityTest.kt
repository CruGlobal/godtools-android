package org.cru.godtools.xml.model

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageGravityTest {
    @Test
    fun verifyParse() {
        ImageGravity.parse("start unrecognized center", null)!!.also { gravity ->
            assertFalse(ImageGravity.isCenterX(gravity))
            assertTrue(ImageGravity.isStart(gravity))
            assertFalse(ImageGravity.isEnd(gravity))
            assertTrue(ImageGravity.isCenterY(gravity))
            assertFalse(ImageGravity.isTop(gravity))
            assertFalse(ImageGravity.isBottom(gravity))
            assertFalse(ImageGravity.isCenter(gravity))
        }

        ImageGravity.parse("center end", null)!!.also { gravity ->
            assertFalse(ImageGravity.isCenterX(gravity))
            assertFalse(ImageGravity.isStart(gravity))
            assertTrue(ImageGravity.isEnd(gravity))
            assertTrue(ImageGravity.isCenterY(gravity))
            assertFalse(ImageGravity.isTop(gravity))
            assertFalse(ImageGravity.isBottom(gravity))
            assertFalse(ImageGravity.isCenter(gravity))
        }

        ImageGravity.parse("center", null)!!.also { gravity ->
            assertTrue(ImageGravity.isCenterX(gravity))
            assertFalse(ImageGravity.isStart(gravity))
            assertFalse(ImageGravity.isEnd(gravity))
            assertTrue(ImageGravity.isCenterY(gravity))
            assertFalse(ImageGravity.isTop(gravity))
            assertFalse(ImageGravity.isBottom(gravity))
            assertTrue(ImageGravity.isCenter(gravity))
        }
    }

    @Test
    fun verifyParseConflictingGravity() {
        assertThat(ImageGravity.parse("start end", null), nullValue())
        assertThat(ImageGravity.parse("start top end", null), nullValue())
        assertThat(ImageGravity.parse("bottom top end", null), nullValue())
    }

    @Test
    fun verifyParseDefaultValue() {
        for (align in intArrayOf(
            ImageGravity.CENTER,
            ImageGravity.START,
            ImageGravity.END,
            ImageGravity.TOP,
            ImageGravity.BOTTOM
        )) {
            assertThat(ImageGravity.parse(null, align), equalTo(align))
            assertThat(ImageGravity.parse("invalid-type", align), equalTo(align))
        }
    }
}
