package org.cru.godtools.base.tool.ui.controller.cache

import org.cru.godtools.xml.model.Image
import org.junit.Assert.assertEquals
import org.junit.Test

class UiControllerTypeTest {
    @Test
    @UiControllerType(Image::class, 13)
    fun testUiControllerTypeAnnotation() {
        val expected = this::testUiControllerTypeAnnotation.annotations.first { it is UiControllerType }
        val autoAnnotation = UiControllerType.create(Image::class, 13)

        assertEquals(expected, autoAnnotation)
        assertEquals(expected.hashCode(), autoAnnotation.hashCode())
    }
}
