package org.cru.godtools.base.tool.ui.controller.cache

import org.cru.godtools.xml.model.Image
import org.junit.Assert.assertEquals
import org.junit.Test

class UiControllerTypeTest {
    @Test
    @UiControllerType(Image::class)
    fun testUiControllerTypeAnnotation() {
        val expected = this::testUiControllerTypeAnnotation.annotations.first { it is UiControllerType }
        val autoAnnotation = UiControllerType.create(Image::class)

        assertEquals(expected, autoAnnotation)
        assertEquals(expected.hashCode(), autoAnnotation.hashCode())
    }
}
