package org.cru.godtools.base.tool.activity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseToolRendererViewModelTest {
    private companion object {
        private const val TOOL = "kgp"
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val manifestManager: ManifestManager = mockk {
        every { getLatestPublishedManifestFlow(any(), any()) } returns flowOf(null)
    }
    private val testScope = TestScope()

    private lateinit var viewModel: BaseToolRendererViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScope.testScheduler))

        viewModel = BaseToolRendererViewModel(
            manifestManager,
            SavedStateHandle()
        )
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    // region Property: manifest
    @Test
    fun `Property manifest - Change Active Locale`() = testScope.runTest {
        val frenchManifest = Manifest(type = Manifest.Type.TRACT)
        every { manifestManager.getLatestPublishedManifestFlow(TOOL, Locale.ENGLISH) } returns flowOf(null)
        every { manifestManager.getLatestPublishedManifestFlow(TOOL, Locale.FRENCH) } returns flowOf(frenchManifest)

        viewModel.toolCode.value = TOOL
        viewModel.locale.value = Locale.ENGLISH
        viewModel.manifest.test {
            assertNull(expectMostRecentItem())

            viewModel.locale.value = Locale.FRENCH
            assertSame(frenchManifest, awaitItem())
        }
    }
    // endregion Property: manifest
}
