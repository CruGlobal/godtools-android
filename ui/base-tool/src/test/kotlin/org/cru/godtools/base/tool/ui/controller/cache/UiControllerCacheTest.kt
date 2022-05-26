package org.cru.godtools.base.tool.ui.controller.cache

import android.content.pm.ApplicationInfo
import android.view.ViewGroup
import io.mockk.Called
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
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
import timber.log.Timber

class UiControllerCacheTest {
    private val parent: ViewGroup = mockk {
        every { context } returns mockk {
            every { applicationInfo } returns ApplicationInfo()
        }
    }
    private val parentController: BaseController<*> = mockk()
    private val imageFactory: BaseController.Factory<BaseController<Image>> = mockk {
        every { create(parent, parentController) } answers { mockk() }
    }
    private val imageFactory2: BaseController.Factory<BaseController<Image>> = mockk {
        every { create(parent, parentController) } answers { mockk() }
    }
    private val variationResolver: VariationResolver = mockk { every { resolve(any()) } returns null }
    private lateinit var cache: UiControllerCache

    @Before
    fun setup() {
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
        val model: Image = mockk()
        val controller: BaseController<Image> = mockk(relaxed = true)
        cache.release(model, controller)
        val controller2 = cache.acquire(model)
        assertSame(controller, controller2)
    }

    @Test
    fun testAcquireDoesntReuseDifferentVariation() {
        val model1: Image = mockk()
        val model2: Image = mockk()
        every { variationResolver.resolve(model1) } returns 1
        every { variationResolver.resolve(model2) } returns 2

        val controller: BaseController<Image> = mockk(relaxed = true)
        cache.release(model1, controller)
        assertThat(cache.acquire(model2), not(sameInstance(controller)))
        assertThat(cache.acquire(model1), sameInstance(controller))
    }
    // endregion acquire()

    // region createController()
    @Test
    fun testCreateController() {
        val model: Image = mockk()
        val controller = cache.acquire(model)
        val controller2 = cache.acquire(model)
        assertNotNull(controller)
        assertNotNull(controller2)
        assertNotSame(controller, controller2)
        verify(exactly = 2) { imageFactory.create(parent, parentController) }
        confirmVerified(imageFactory, imageFactory2)
    }

    @Test
    fun testCreateControllerVariations() {
        val model: Image = mockk()
        every { variationResolver.resolve(model) } returns 2

        val controller = cache.acquire(model)
        assertNotNull(controller)
        verifyAll {
            imageFactory2.create(parent, parentController)
            imageFactory wasNot Called
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateControllerFactoryMissingDebug() {
        parent.context.applicationInfo.flags = ApplicationInfo.FLAG_DEBUGGABLE
        cache.acquire(mockk<Text>())
    }

    @Test
    fun testCreateControllerFactoryMissingRelease() {
        val tree: Timber.Tree = mockk(relaxed = true)
        Timber.plant(tree)

        try {
            val controller = cache.acquire(mockk<Text>())
            assertNull(controller)
            verify { tree.e(any<IllegalArgumentException>(), any(), *anyVararg()) }
            confirmVerified(imageFactory, imageFactory2)
        } finally {
            Timber.uproot(tree)
        }
    }
    // endregion createController()
}
