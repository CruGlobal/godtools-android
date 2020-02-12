package org.cru.godtools.xml.model

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.sameInstance
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

    @Test
    fun verifyParse() {
        val defValue = mutableSetOf<DeviceType>()
        assertThat(DeviceType.parse(null, defValue), sameInstance(defValue))
        assertThat(DeviceType.parse("aljksdf ajklsdfa awe", defValue), containsInAnyOrder(DeviceType.UNKNOWN))
        assertThat(
            DeviceType.parse("${DeviceType.XML_DEVICE_TYPE_MOBILE} ajklsdfa awe", defValue),
            containsInAnyOrder(DeviceType.MOBILE, DeviceType.UNKNOWN)
        )
    }
}
