package org.cru.godtools.base.ui.util

import org.cru.godtools.model.Translation
import org.cru.godtools.model.randomTool
import org.cru.godtools.model.randomTranslation
import org.junit.Assert.assertEquals
import org.junit.Test

class ModelUtilsTest {
    private val tool = randomTool {
        name = "tool"
        description = "tool description"
    }
    private val translation = randomTranslation(
        name = "translation",
        description = "translation description",
    )
    private val translationNull: Translation? = null

    @Test
    fun testTranslationGetName() {
        assertEquals("", translationNull.getName(null, null))
        assertEquals("tool", translationNull.getName(tool, null))
        assertEquals("translation", translation.getName(null, null))
        assertEquals("translation", translation.getName(tool, null))
    }

    @Test
    fun testTranslationGetDescription() {
        assertEquals("", translationNull.getDescription(null, null))
        assertEquals("tool description", translationNull.getDescription(tool, null))
        assertEquals("translation description", translation.getDescription(null, null))
        assertEquals("translation description", translation.getDescription(tool, null))
    }
}
