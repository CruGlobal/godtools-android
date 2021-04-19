package org.cru.godtools.xml.model

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageGravityTest {
    private val none = ImageGravity(0)

    @Test
    fun verifyParse() {
        ImageGravity.parse("start unrecognized center", ImageGravity.NONE).also { gravity ->
            assertFalse(gravity.isCenterX)
            assertTrue(gravity.isStart)
            assertFalse(gravity.isEnd)
            assertTrue(gravity.isCenterY)
            assertFalse(gravity.isTop)
            assertFalse(gravity.isBottom)
            assertFalse(gravity.isCenter)
        }

        ImageGravity.parse("center end", ImageGravity.NONE).also { gravity ->
            assertFalse(gravity.isCenterX)
            assertFalse(gravity.isStart)
            assertTrue(gravity.isEnd)
            assertTrue(gravity.isCenterY)
            assertFalse(gravity.isTop)
            assertFalse(gravity.isBottom)
            assertFalse(gravity.isCenter)
        }

        ImageGravity.parse("center", ImageGravity.NONE).also { gravity ->
            assertTrue(gravity.isCenterX)
            assertFalse(gravity.isStart)
            assertFalse(gravity.isEnd)
            assertTrue(gravity.isCenterY)
            assertFalse(gravity.isTop)
            assertFalse(gravity.isBottom)
            assertTrue(gravity.isCenter)
        }
    }

    @Test
    fun verifyParseConflictingGravity() {
        assertEquals(none, ImageGravity.parse("start end", none))
        assertEquals(none, ImageGravity.parse("start top end", none))
        assertEquals(none, ImageGravity.parse("bottom top end", none))
    }

    @Test
    fun verifyParseDefaultValue() {
        arrayOf(
            ImageGravity.START,
            ImageGravity.END,
            ImageGravity.TOP,
            ImageGravity.BOTTOM,
            ImageGravity.CENTER
        ).forEach {
            assertThat(ImageGravity.parse(null, it), equalTo(it))
            assertThat(ImageGravity.parse("invalid-type", it), equalTo(it))
        }
    }
}
