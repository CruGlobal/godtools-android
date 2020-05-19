package org.cru.godtools.tract.activity

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.base.tool.activity.BaseToolActivity.Companion.STATE_INVALID_TYPE
import org.cru.godtools.base.tool.activity.BaseToolActivity.Companion.STATE_LOADED
import org.cru.godtools.base.tool.activity.BaseToolActivity.Companion.STATE_LOADING
import org.cru.godtools.base.tool.activity.BaseToolActivity.Companion.STATE_NOT_FOUND
import org.cru.godtools.model.Translation
import org.cru.godtools.tract.activity.KotlinTractActivity.Companion.determineState
import org.cru.godtools.xml.model.Manifest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TractActivityTest {
    @Test
    fun verifyDetermineState() {
        assertEquals(STATE_LOADED, determineState(Manifest().apply { mType = Manifest.Type.TRACT }, null, false))
        assertEquals(STATE_INVALID_TYPE, determineState(Manifest().apply { mType = Manifest.Type.ARTICLE }, null, false))
        assertEquals(STATE_LOADING, determineState(null, null, false))
        assertEquals(STATE_LOADING, determineState(null, Translation(), true))
        assertEquals(STATE_NOT_FOUND, determineState(null, null, true))
    }
}
