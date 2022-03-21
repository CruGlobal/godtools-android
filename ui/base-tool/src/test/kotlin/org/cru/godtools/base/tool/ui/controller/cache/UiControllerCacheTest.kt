package org.cru.godtools.base.tool.ui.controller.cache

import android.content.pm.ApplicationInfo
import android.view.ViewGroup
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.tool.model.Image
import org.cru.godtools.tool.model.Text
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.sameInstance
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import timber.log.Timber

class UiControllerCacheTest {
    private lateinit var parent: ViewGroup
    private lateinit var parentController: BaseController<*>
    private lateinit var imageFactory: BaseController.Factory<BaseController<Image>>
    private lateinit var imageFactory2: BaseController.Factory<BaseController<Image>>
    private lateinit var variationResolver: VariationResolver
    private lateinit var cache: UiControllerCache

    @Before
    fun setup() {
        parent = mock(defaultAnswer = RETURNS_DEEP_STUBS)
        parentController = mock()
        imageFactory = mock { on { create(parent, parentController) } doAnswer { mock() } }
        imageFactory2 = mock { on { create(parent, parentController) } doAnswer { mock() } }
        variationResolver = mock { on { resolve(any()) } doReturn null }
        cache = UiControllerCache(
            parent,
            parentController,
            mapOf(
                UiControllerType.create(Image::class) to imageFactory,
                UiControllerType.create(Image::class, 2) to imageFactory2
            ),
            setOf(variationResolver)
        )
    }

    // region acquire()
    @Test
    fun testAcquireReusesReleased() {
        val model = mock<Image>()
        val image = mock<BaseController<Image>>()
        cache.release(model, image)
        val image2 = cache.acquire(model)
        assertSame(image, image2)
    }

    @Test
    fun testAcquireDoesntReuseDifferentVariation() {
        val model1 = mock<Image>()
        val model2 = mock<Image>()
        variationResolver.stub {
            on { resolve(model1) } doReturn 1
            on { resolve(model2) } doReturn 2
        }

        val image = mock<BaseController<Image>>()
        cache.release(model1, image)
        assertThat(cache.acquire(model2), not(sameInstance(image)))
        assertThat(cache.acquire(model1), sameInstance(image))
    }
    // endregion acquire()

    // region createController()
    @Test
    fun testCreateController() {
        val model = mock<Image>()
        val controller = cache.acquire(model)
        assertNotNull(controller)
        verify(imageFactory).create(parent, parentController)
        verifyNoInteractions(imageFactory2)
        verifyNoMoreInteractions(imageFactory)
        clearInvocations(imageFactory)

        val controller2 = cache.acquire(model)
        assertNotNull(controller2)
        assertNotSame(controller, controller2)
        verify(imageFactory).create(parent, parentController)
        verifyNoInteractions(imageFactory2)
        verifyNoMoreInteractions(imageFactory)
    }

    @Test
    fun testCreateControllerVariations() {
        val model = mock<Image>()
        whenever(variationResolver.resolve(model)).thenReturn(2)

        val controller = cache.acquire(model)
        assertNotNull(controller)
        verify(imageFactory2).create(parent, parentController)
        verifyNoInteractions(imageFactory)
        verifyNoMoreInteractions(imageFactory2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateControllerFactoryMissingDebug() {
        parent.context.applicationInfo.flags = ApplicationInfo.FLAG_DEBUGGABLE
        cache.acquire(mock<Text>())
    }

    @Test
    fun testCreateControllerFactoryMissingRelease() {
        val tree = mock<Timber.Tree>()
        Timber.plant(tree)

        try {
            val controller = cache.acquire(mock<Text>())
            assertNull(controller)
            verifyNoInteractions(imageFactory)
            verify(tree).e(any<IllegalArgumentException>(), any(), anyVararg())
        } finally {
            Timber.uproot(tree)
        }
    }
    // endregion createController()
}
