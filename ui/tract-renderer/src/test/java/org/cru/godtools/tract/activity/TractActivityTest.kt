package org.cru.godtools.tract.activity

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.base.tool.activity.BaseToolActivity.ToolState
import org.cru.godtools.model.Translation
import org.cru.godtools.tract.activity.TractActivity.Companion.determineState
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Manifest.Type
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TractActivityTest {
    @Test
    fun verifyDetermineState() {
        assertEquals(ToolState.LOADED, determineState(Manifest().apply { mType = Type.TRACT }, null, false))
        assertEquals(ToolState.INVALID_TYPE, determineState(Manifest().apply { mType = Type.ARTICLE }, null, false))
        assertEquals(ToolState.LOADING, determineState(null, null, false))
        assertEquals(ToolState.LOADING, determineState(null, Translation(), true))
        assertEquals(ToolState.NOT_FOUND, determineState(null, null, true))
    }
}
