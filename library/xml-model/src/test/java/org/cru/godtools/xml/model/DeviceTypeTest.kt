package org.cru.godtools.xml.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeviceTypeTest {
    @Test
    fun verifyParseSingle() {
        assertNull(DeviceType.parseSingle(null))
        assertEquals(DeviceType.UNKNOWN, DeviceType.parseSingle("hjasdf"))
        assertEquals(DeviceType.MOBILE, DeviceType.parseSingle(DeviceType.XML_DEVICE_TYPE_MOBILE))
    }
}
