package org.cru.godtools.base.tool.ui.controller

import android.content.pm.ApplicationInfo
import android.view.ViewGroup
import com.nhaarman.mockitokotlin2.anyVararg
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.cru.godtools.xml.model.Text
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.any
import timber.log.Timber

class UiControllerCacheTest {
    private lateinit var parent: ViewGroup
    private lateinit var parentController: BaseController<*>
    private lateinit var cache: UiControllerCache

    @Before
    fun setup() {
        parent = mock(defaultAnswer = RETURNS_DEEP_STUBS)
        parentController = mock()
        cache = UiControllerCache(parent, parentController, emptyMap())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateControllerFactoryMissingDebug() {
        parent.context.applicationInfo.flags = ApplicationInfo.FLAG_DEBUGGABLE
        cache.acquire(Text::class)
    }

    @Test
    fun testCreateControllerFactoryMissingRelease() {
        val tree = mock<Timber.Tree>()
        Timber.plant(tree)

        try {
            val controller = cache.acquire(Text::class)
            assertNull(controller)
            verify(tree).e(any<IllegalArgumentException>(), any(), anyVararg())
        } finally {
            Timber.uproot(tree)
        }
    }
}
