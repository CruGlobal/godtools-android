package org.cru.godtools.base.tool.activity

import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.EnumSet
import org.cru.godtools.base.tool.activity.BaseToolActivity.LoadingState
import org.cru.godtools.base.tool.activity.BaseToolActivity.LoadingState.INVALID_TYPE
import org.cru.godtools.base.tool.activity.BaseToolActivity.LoadingState.LOADED
import org.cru.godtools.base.tool.activity.BaseToolActivity.LoadingState.LOADING
import org.cru.godtools.base.tool.activity.BaseToolActivity.LoadingState.NOT_FOUND
import org.cru.godtools.base.tool.activity.BaseToolActivity.LoadingState.OFFLINE
import org.cru.godtools.model.Translation
import org.cru.godtools.tool.model.Manifest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoadingStateTest {
    @Test
    fun verifyDetermineToolState() {
        val types = EnumSet.allOf(Manifest.Type::class.java) - Manifest.Type.UNKNOWN
        types.forEach { validType ->
            val manifest = Manifest(type = validType)
            assertEquals(LOADED, LoadingState.determineToolState(manifest, null))
            assertEquals(LOADED, LoadingState.determineToolState(manifest, null, manifestType = validType))
            EnumSet.complementOf(EnumSet.of(validType)).forEach { invalidType ->
                assertEquals(INVALID_TYPE, LoadingState.determineToolState(manifest, null, manifestType = invalidType))
            }
        }

        assertEquals(NOT_FOUND, LoadingState.determineToolState(null, null, isSyncFinished = true, isConnected = true))
        assertEquals(NOT_FOUND, LoadingState.determineToolState(null, null, isSyncFinished = true, isConnected = false))
        assertEquals(LOADING, LoadingState.determineToolState(null, null, isSyncFinished = false))
        assertEquals(LOADING, LoadingState.determineToolState(null, Translation(), isSyncFinished = true))
        assertEquals(OFFLINE, LoadingState.determineToolState(null, null, isSyncFinished = false, isConnected = false))
    }
}
