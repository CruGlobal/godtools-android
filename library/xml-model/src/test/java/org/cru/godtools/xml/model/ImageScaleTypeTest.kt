package org.cru.godtools.xml.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageScaleTypeTest {
    @Test
    fun testParseString() {
        assertEquals(ImageScaleType.FIT, Utils.parseScaleType("fit", null))
        assertEquals(ImageScaleType.FILL, Utils.parseScaleType("fill", null))
        assertEquals(ImageScaleType.FILL_X, Utils.parseScaleType("fill-x", null))
        assertEquals(ImageScaleType.FILL_Y, Utils.parseScaleType("fill-y", null))
    }

    @Test
    fun testParseStringDefaultValue() {
        assertNull(Utils.parseScaleType(null, null))
        assertEquals(ImageScaleType.FIT, Utils.parseScaleType(null, ImageScaleType.FIT))
        assertEquals(ImageScaleType.FIT, Utils.parseScaleType("ajklsdfjkaewr", ImageScaleType.FIT))
    }
}
