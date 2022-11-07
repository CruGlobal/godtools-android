package org.cru.godtools.base.tool.activity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.Called
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.user.activity.UserCounterNames
import org.cru.godtools.user.activity.UserActivityManager
import org.junit.Rule

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
    private val userActivityManager: UserActivityManager = mockk(relaxUnitFun = true)
    private val testScope = TestScope()

    private lateinit var viewModel: BaseToolRendererViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScope.testScheduler))

        viewModel = BaseToolRendererViewModel(
            mockk(),
            manifestManager,
            userActivityManager,
            SavedStateHandle()
        )
    }

    @AfterTest
    fun cleanup() {
        Dispatchers.resetMain()
    }

    // region Property: manifest
    @Test
    fun `Property manifest - supportedType constraint`() = testScope.runTest {
        val manifestFlow = MutableStateFlow<Manifest?>(null)
        every { manifestManager.getLatestPublishedManifestFlow(TOOL, Locale.ENGLISH) } returns manifestFlow

        viewModel.manifest.test {
            viewModel.supportedType.value = Manifest.Type.TRACT
            viewModel.toolCode.value = TOOL
            viewModel.locale.value = Locale.ENGLISH
            manifestFlow.value = null
            assertNull(expectMostRecentItem())

            manifestFlow.value = Manifest(type = Manifest.Type.TRACT)
            assertEquals(Manifest.Type.TRACT, assertNotNull(expectMostRecentItem()).type)

            manifestFlow.value = Manifest(type = Manifest.Type.CYOA)
            assertNull(expectMostRecentItem())

            viewModel.supportedType.value = Manifest.Type.CYOA
            assertEquals(Manifest.Type.CYOA, assertNotNull(expectMostRecentItem()).type)
        }
    }

    @Test
    fun `Property manifest - supportedType null`() = testScope.runTest {
        val manifestFlow = MutableStateFlow<Manifest?>(null)
        every { manifestManager.getLatestPublishedManifestFlow(TOOL, Locale.ENGLISH) } returns manifestFlow

        viewModel.manifest.test {
            viewModel.supportedType.value = null
            viewModel.toolCode.value = TOOL
            viewModel.locale.value = Locale.ENGLISH
            manifestFlow.value = null
            assertNull(expectMostRecentItem())

            manifestFlow.value = Manifest(type = Manifest.Type.TRACT)
            assertEquals(Manifest.Type.TRACT, assertNotNull(expectMostRecentItem()).type)

            manifestFlow.value = Manifest(type = Manifest.Type.CYOA)
            assertEquals(Manifest.Type.CYOA, assertNotNull(expectMostRecentItem()).type)
        }
    }

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

    // region Job: Tool Language Used
    @Test
    fun `Job Tool Language Used`() = testScope.runTest {
        val manifestFlow = MutableStateFlow<Manifest?>(null)
        every { manifestManager.getLatestPublishedManifestFlow(TOOL, any()) } returns manifestFlow
        viewModel.toolCode.value = TOOL
        viewModel.locale.value = Locale("")
        verify { userActivityManager wasNot Called }

        manifestFlow.value = Manifest(locale = Locale.ENGLISH)
        coVerify { userActivityManager.updateCounter(UserCounterNames.LANGUAGE_USED(Locale.ENGLISH)) }
        confirmVerified(userActivityManager)

        manifestFlow.value = Manifest(locale = Locale.FRENCH)
        coVerify { userActivityManager.updateCounter(UserCounterNames.LANGUAGE_USED(Locale.FRENCH)) }
        confirmVerified(userActivityManager)
    }

    @Test
    fun `Job Tool Language Used - Record Unique Languages Only`() = testScope.runTest {
        val manifestFlow = MutableStateFlow<Manifest?>(null)
        every { manifestManager.getLatestPublishedManifestFlow(TOOL, any()) } returns manifestFlow
        viewModel.toolCode.value = TOOL
        viewModel.locale.value = Locale("")
        verify { userActivityManager wasNot Called }

        manifestFlow.value = Manifest(locale = Locale.ENGLISH)
        manifestFlow.value = Manifest(locale = Locale.FRENCH)
        manifestFlow.value = Manifest(locale = Locale.FRENCH)
        manifestFlow.value = Manifest(locale = Locale.ENGLISH)
        coVerify(exactly = 1) {
            userActivityManager.updateCounter(UserCounterNames.LANGUAGE_USED(Locale.ENGLISH))
            userActivityManager.updateCounter(UserCounterNames.LANGUAGE_USED(Locale.FRENCH))
        }
        confirmVerified(userActivityManager)
    }
    // endregion Job: Tool Language Used
}
