package org.cru.godtools.shortcuts

import android.content.Context
import android.content.pm.ShortcutManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.cru.godtools.base.Settings
import org.cru.godtools.model.Tool
import org.greenrobot.eventbus.EventBus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.db.GodToolsDao
import java.util.EnumSet

@RunWith(AndroidJUnit4::class)
class GodToolsShortcutManagerTest {
    private lateinit var context: Context
    private lateinit var systemShortcutManager: ShortcutManager
    private lateinit var dao: GodToolsDao
    private lateinit var eventBus: EventBus
    private lateinit var settings: Settings

    private lateinit var shortcutManager: GodToolsShortcutManager

    @Before
    fun setup() {
        systemShortcutManager = mock {
            whenever(it.isRequestPinShortcutSupported) doReturn true
        }
        context = mock {
            whenever(it.getSystemService(ShortcutManager::class.java)) doReturn systemShortcutManager
        }
        dao = mock()
        eventBus = mock()
        settings = mock()

        shortcutManager = GodToolsShortcutManager(context, dao, eventBus, settings)
    }

    @Test
    fun verifyCanPinToolShortcut() {
        val supportedTypes = EnumSet.of(Tool.Type.TRACT, Tool.Type.ARTICLE)
        Tool.Type.values().forEach {
            assertEquals(supportedTypes.contains(it), shortcutManager.canPinToolShortcut(Tool().apply { type = it }))
        }
    }

    @Test
    fun verifyCanPinToolShortcutNull() {
        assertFalse(shortcutManager.canPinToolShortcut(null))
    }
}
