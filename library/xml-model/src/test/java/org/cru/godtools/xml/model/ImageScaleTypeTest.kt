package org.cru.godtools.xml.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageScaleTypeTest {
    @Test
    fun testParseString() {
        assertEquals(ImageScaleType.FIT, ImageScaleType.parse("fit", null))
        assertEquals(ImageScaleType.FILL, ImageScaleType.parse("fill", null))
        assertEquals(ImageScaleType.FILL_X, ImageScaleType.parse("fill-x", null))
        assertEquals(ImageScaleType.FILL_Y, ImageScaleType.parse("fill-y", null))
    }

    @Test
    fun testParseStringDefaultValue() {
        assertNull(ImageScaleType.parse(null, null))
        assertEquals(ImageScaleType.FIT, ImageScaleType.parse(null, ImageScaleType.FIT))
        assertEquals(ImageScaleType.FIT, ImageScaleType.parse("ajklsdfjkaewr", ImageScaleType.FIT))
    }
}
