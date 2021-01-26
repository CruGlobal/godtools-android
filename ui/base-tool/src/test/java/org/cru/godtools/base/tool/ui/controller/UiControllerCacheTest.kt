package org.cru.godtools.base.tool.ui.controller

import android.content.pm.ApplicationInfo
import android.view.ViewGroup
import com.nhaarman.mockitokotlin2.anyVararg
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.cru.godtools.xml.model.Image
import org.cru.godtools.xml.model.Text
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.any
import org.mockito.Mockito.verifyNoInteractions
import timber.log.Timber

class UiControllerCacheTest {
    private lateinit var parent: ViewGroup
    private lateinit var parentController: BaseController<*>
    private lateinit var imageFactory: BaseController.Factory<BaseController<Image>>
    private lateinit var cache: UiControllerCache

    @Before
    fun setup() {
        parent = mock(defaultAnswer = RETURNS_DEEP_STUBS)
        parentController = mock()
        imageFactory = mock { on { create(parent, parentController) } doAnswer { mock() } }
        cache = UiControllerCache(parent, parentController, mapOf(Image::class.java to imageFactory))
    }

    // region createController()
    @Test
    fun testCreateController() {
        val controller = cache.acquire(Image::class)
        assertNotNull(controller)
        verify(imageFactory).create(parent, parentController)
        verifyNoMoreInteractions(imageFactory)
        clearInvocations(imageFactory)

        val controller2 = cache.acquire(Image::class)
        assertNotNull(controller2)
        assertNotSame(controller, controller2)
        verify(imageFactory).create(parent, parentController)
        verifyNoMoreInteractions(imageFactory)
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
            verifyNoInteractions(imageFactory)
            verify(tree).e(any<IllegalArgumentException>(), any(), anyVararg())
        } finally {
            Timber.uproot(tree)
        }
    }
    // endregion createController()
}
