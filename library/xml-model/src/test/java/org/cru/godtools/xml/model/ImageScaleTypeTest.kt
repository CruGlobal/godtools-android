package org.cru.godtools.xml.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageScaleTypeTest {
    @Test
    fun testParseOrNull() {
        assertEquals(ImageScaleType.FIT, ImageScaleType.parseOrNull("fit"))
        assertEquals(ImageScaleType.FILL, ImageScaleType.parseOrNull("fill"))
        assertEquals(ImageScaleType.FILL_X, ImageScaleType.parseOrNull("fill-x"))
        assertEquals(ImageScaleType.FILL_Y, ImageScaleType.parseOrNull("fill-y"))
        assertNull(ImageScaleType.parseOrNull(null))
        assertNull(ImageScaleType.parseOrNull("ajklsdfjkaewr"))
    }
}
