package org.cru.godtools.xml.model

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeviceTypeTest {
    @Test
    fun verifyParseSingle() {
        assertNull(DeviceType.parseSingle(null))
        assertEquals(DeviceType.UNKNOWN, DeviceType.parseSingle("hjasdf"))
        assertEquals(DeviceType.ANDROID, DeviceType.parseSingle(DeviceType.XML_DEVICE_TYPE_ANDROID))
        assertEquals(DeviceType.MOBILE, DeviceType.parseSingle(DeviceType.XML_DEVICE_TYPE_MOBILE))
    }

    @Test
    fun verifyParse() {
        assertNull(DeviceType.parseOrNull(null))
        assertThat(DeviceType.parseOrNull("aljksdf ajklsdfa awe"), contains(DeviceType.UNKNOWN))
        assertThat(
            DeviceType.parseOrNull("${DeviceType.XML_DEVICE_TYPE_MOBILE} ajklsdfa awe"),
            containsInAnyOrder(DeviceType.MOBILE, DeviceType.UNKNOWN)
        )
        assertThat(
            DeviceType.parseOrNull("${DeviceType.XML_DEVICE_TYPE_ANDROID} ${DeviceType.XML_DEVICE_TYPE_MOBILE} aw"),
            containsInAnyOrder(DeviceType.ANDROID, DeviceType.MOBILE, DeviceType.UNKNOWN)
        )
    }
}
