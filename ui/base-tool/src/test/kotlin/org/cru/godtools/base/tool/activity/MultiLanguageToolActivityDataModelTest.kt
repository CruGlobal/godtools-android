package org.cru.godtools.base.tool.activity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyAll
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.cru.godtools.base.tool.activity.BaseToolActivity.LoadingState
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Translation
import org.cru.godtools.model.randomTranslation
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.aMapWithSize
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anEmptyMap
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.hasEntry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val TOOL = "kgp"

@OptIn(ExperimentalCoroutinesApi::class)
class MultiLanguageToolActivityDataModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // region Objects & Mocks
    private val manifestManager: ManifestManager = mockk {
        every { getLatestPublishedManifestFlow(any(), any()) } returns flowOf()
    }
    private lateinit var dataModel: MultiLanguageToolActivityDataModel
    private val isConnnected = MutableLiveData(true)
    private val savedStateHandle = SavedStateHandle()
    private val toolsRepository: ToolsRepository = mockk {
        every { findToolFlow(any()) } returns flowOf(null)
    }
    private val translationsRepository: TranslationsRepository = mockk {
        every { findLatestTranslationFlow(any(), any()) } returns emptyFlow()
    }
    private val testScope = TestScope()

    @Before
    fun setupDataModel() {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScope.testScheduler))
        dataModel = MultiLanguageToolActivityDataModel(
            mockk(),
            mockk(),
            manifestManager,
            toolsRepository,
            translationsRepository,
            mockk(),
            isConnnected,
            savedStateHandle
        )
    }

    @After
    fun cleanupDataModel() {
        Dispatchers.resetMain()
    }

    private fun everyGetManifest(tool: String, locale: Locale) =
        every { manifestManager.getLatestPublishedManifestFlow(tool, locale) }
    private fun everyGetTranslation(tool: String, locale: Locale) =
        every { translationsRepository.findLatestTranslationFlow(tool, locale) }
    // endregion Objects & Mocks

    // region Resolved Data
    // region Property: translations
    @Test
    fun `Property translations`() {
        val translation = randomTranslation()
        everyGetTranslation(TOOL, Locale.ENGLISH) returns flowOf(null)
        everyGetTranslation(TOOL, Locale.FRENCH) returns emptyFlow()
        everyGetTranslation(TOOL, Locale.CHINESE) returns flowOf(translation)
        val observer = mockk<Observer<Map<Locale, Translation?>>>(relaxUnitFun = true)
        dataModel.toolCode.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH, Locale.CHINESE)

        dataModel.translations.observeForever(observer)
        verify {
            translationsRepository.findLatestTranslationFlow(TOOL, Locale.ENGLISH)
            translationsRepository.findLatestTranslationFlow(TOOL, Locale.FRENCH)
            translationsRepository.findLatestTranslationFlow(TOOL, Locale.CHINESE)
            observer.onChanged(
                withArg {
                    assertThat(
                        it,
                        allOf(
                            aMapWithSize(3),
                            hasEntry<Locale, Translation?>(Locale.ENGLISH, null),
                            hasEntry<Locale, Translation?>(Locale.FRENCH, null),
                            hasEntry(Locale.CHINESE, translation)
                        )
                    )
                }
            )
        }
        confirmVerified(observer)
    }

    @Test
    fun `Property translations - No Locales`() {
        val observer = mockk<Observer<Map<Locale, Translation?>>>(relaxUnitFun = true)
        dataModel.translations.observeForever(observer)
        assertThat(dataModel.translations.value, anEmptyMap())
        dataModel.toolCode.value = TOOL
        assertThat(dataModel.translations.value, anEmptyMap())
        verifyAll { observer.onChanged(eq(emptyMap())) }
    }

    @Test
    fun `Property translations - Update Translation`() {
        val french = MutableStateFlow<Translation?>(null)
        val translations = mutableListOf<Map<Locale, Translation?>>()
        val observer = mockk<Observer<Map<Locale, Translation?>>> {
            every { onChanged(capture(translations)) } returns Unit
        }
        everyGetTranslation(TOOL, Locale.ENGLISH) returns flowOf(null)
        everyGetTranslation(TOOL, Locale.FRENCH) returns french
        dataModel.toolCode.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)

        dataModel.translations.observeForever(observer)
        french.value = randomTranslation()
        verify {
            translationsRepository.findLatestTranslationFlow(TOOL, Locale.ENGLISH)
            translationsRepository.findLatestTranslationFlow(TOOL, Locale.FRENCH)
            observer.onChanged(any())
        }
        assertThat(
            translations,
            contains(
                allOf(
                    aMapWithSize(2),
                    hasEntry<Locale, Translation?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Translation?>(Locale.FRENCH, null)
                ),
                allOf(
                    aMapWithSize(2),
                    hasEntry<Locale, Translation?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Translation?>(Locale.FRENCH, french.value)
                )
            )
        )
    }
    // endregion Property: translations

    // region Property: manifests
    @Test
    fun `Property manifests`() {
        val observer = mockk<Observer<Map<Locale, Manifest?>>>(relaxUnitFun = true)
        every { manifestManager.getLatestPublishedManifestFlow(TOOL, Locale.ENGLISH) } returns flowOf(null)
        everyGetManifest(TOOL, Locale.ENGLISH) returns flowOf(null)
        every { manifestManager.getLatestPublishedManifestFlow(TOOL, Locale.FRENCH) } returns flowOf(null)
        everyGetManifest(TOOL, Locale.FRENCH) returns flowOf()
        dataModel.toolCode.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)

        dataModel.manifests.observeForever(observer)
        verifyAll {
            observer.onChanged(
                withArg {
                    assertThat(
                        it,
                        allOf(
                            aMapWithSize(2),
                            hasEntry<Locale, Manifest?>(Locale.ENGLISH, null),
                            hasEntry<Locale, Manifest?>(Locale.FRENCH, null)
                        )
                    )
                }
            )
        }
    }

    @Test
    fun `Property manifests - No Locales`() {
        val observer = mockk<Observer<Map<Locale, Manifest?>>>(relaxUnitFun = true)
        dataModel.manifests.observeForever(observer)
        assertThat(dataModel.manifests.value, anEmptyMap())
        dataModel.toolCode.value = TOOL
        assertThat(dataModel.manifests.value, anEmptyMap())
        verifyAll { observer.onChanged(emptyMap()) }
    }

    @Test
    fun `Property manifests - Update Locales`() = testScope.runTest {
        val french = MutableStateFlow(Manifest())
        val manifests = slot<Map<Locale, Manifest?>>()
        val observer = mockk<Observer<Map<Locale, Manifest?>>> {
            every { onChanged(capture(manifests)) } returns Unit
        }
        everyGetManifest(TOOL, Locale.ENGLISH) returns flowOf(null)
        everyGetManifest(TOOL, Locale.FRENCH) returns french
        dataModel.toolCode.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)

        dataModel.manifests.observeForever(observer)
        runCurrent()
        assertThat(
            manifests.captured,
            allOf(
                aMapWithSize(2),
                hasEntry<Locale, Manifest?>(Locale.ENGLISH, null),
                hasEntry<Locale, Manifest?>(Locale.FRENCH, french.value)
            )
        )

        dataModel.primaryLocales.value = listOf(Locale.FRENCH)
        runCurrent()
        verify { observer.onChanged(any()) }
        assertThat(
            manifests.captured,
            allOf(aMapWithSize(1), hasEntry<Locale, Manifest?>(Locale.FRENCH, french.value))
        )
    }

    @Test
    fun `Property manifests - Update Manifest`() {
        val french = MutableStateFlow<Manifest?>(null)
        val manifests = mutableListOf<Map<Locale, Manifest?>>()
        val observer = mockk<Observer<Map<Locale, Manifest?>>> {
            every { onChanged(capture(manifests)) } returns Unit
        }
        everyGetManifest(TOOL, Locale.ENGLISH) returns flowOf(null)
        everyGetManifest(TOOL, Locale.FRENCH) returns french
        dataModel.toolCode.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)
        dataModel.manifests.observeForever(observer)

        french.value = Manifest()
        verifyAll {
            repeat(2) { observer.onChanged(any()) }
        }
        assertThat(
            manifests,
            contains(
                allOf(
                    aMapWithSize(2),
                    hasEntry<Locale, Manifest?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Manifest?>(Locale.FRENCH, null)
                ),
                allOf(
                    aMapWithSize(2),
                    hasEntry<Locale, Manifest?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Manifest?>(Locale.FRENCH, french.value)
                )
            )
        )
    }
    // endregion Property: manifests
    // endregion Resolved Data

    // region Property: loadingState
    @Test
    fun `Property loadingState - Update Translation`() {
        val state = mutableListOf<Map<Locale, LoadingState>>()
        val observer = mockk<Observer<Map<Locale, LoadingState>>> {
            every { onChanged(capture(state)) } returns Unit
        }
        val translation = MutableStateFlow<Translation?>(null)
        everyGetTranslation(TOOL, Locale.ENGLISH) returns translation
        dataModel.toolCode.value = TOOL
        dataModel.supportedType.value = null
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH)
        dataModel.isInitialSyncFinished.value = true

        dataModel.loadingState.observeForever(observer)
        assertThat(
            dataModel.loadingState.value,
            allOf(aMapWithSize(1), hasEntry(Locale.ENGLISH, LoadingState.NOT_FOUND))
        )
        translation.value = randomTranslation()
        assertThat(dataModel.loadingState.value, allOf(aMapWithSize(1), hasEntry(Locale.ENGLISH, LoadingState.LOADING)))
        verifyAll { repeat(2) { observer.onChanged(any()) } }
        assertThat(
            state,
            contains(
                allOf(aMapWithSize(1), hasEntry(Locale.ENGLISH, LoadingState.NOT_FOUND)),
                allOf(aMapWithSize(1), hasEntry(Locale.ENGLISH, LoadingState.LOADING))
            )
        )
    }
    // endregion Property: loadingState

    // region Property: activeLocale
    @Test
    fun `Property activeLocale - Initialize when locales initialized`() {
        assertNull(dataModel.activeLocale.value)
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH)
        assertEquals(Locale.ENGLISH, dataModel.activeLocale.value)
    }

    @Test
    fun `Property activeLocale - Updates when activeLoadingState is INVALID_TYPE`() = testScope.runTest {
        val englishManifest = MutableSharedFlow<Manifest?>()
        everyGetTranslation(TOOL, Locale.ENGLISH) returns flowOf(randomTranslation())
        everyGetTranslation(TOOL, Locale.FRENCH) returns flowOf(randomTranslation())
        everyGetManifest(TOOL, Locale.ENGLISH) returns englishManifest
        everyGetManifest(TOOL, Locale.FRENCH) returns flowOf(Manifest(type = Manifest.Type.TRACT))
        dataModel.toolCode.value = TOOL
        dataModel.supportedType.value = Manifest.Type.TRACT
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)
        dataModel.isInitialSyncFinished.value = true

        assertEquals(Locale.ENGLISH, dataModel.activeLocale.value)
        englishManifest.emit(Manifest(type = Manifest.Type.LESSON))
        assertEquals(Locale.FRENCH, dataModel.activeLocale.value)
    }

    @Test
    fun `Property activeLocale - Updates when activeLoadingState is NOT_FOUND`() = testScope.runTest {
        val englishTranslation = MutableSharedFlow<Translation?>()
        everyGetTranslation(TOOL, Locale.ENGLISH) returns englishTranslation
        everyGetTranslation(TOOL, Locale.FRENCH) returns flowOf(randomTranslation())
        everyGetManifest(TOOL, Locale.ENGLISH) returns flowOf(null)
        everyGetManifest(TOOL, Locale.FRENCH) returns flowOf(Manifest(type = Manifest.Type.TRACT))
        dataModel.toolCode.value = TOOL
        dataModel.supportedType.value = Manifest.Type.TRACT
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)
        dataModel.isInitialSyncFinished.value = true

        assertEquals(Locale.ENGLISH, dataModel.activeLocale.value)
        englishTranslation.emit(null)
        assertEquals(Locale.FRENCH, dataModel.activeLocale.value)
    }

    @Test
    fun `Property activeLocale - Updates when activeLoadingState is OFFLINE`() {
        everyGetTranslation(TOOL, Locale.ENGLISH) returns flowOf(randomTranslation())
        everyGetTranslation(TOOL, Locale.FRENCH) returns flowOf(randomTranslation())
        everyGetManifest(TOOL, Locale.ENGLISH) returns flowOf(null)
        everyGetManifest(TOOL, Locale.FRENCH) returns flowOf(Manifest(type = Manifest.Type.TRACT))
        dataModel.toolCode.value = TOOL
        dataModel.supportedType.value = Manifest.Type.TRACT
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)
        dataModel.isInitialSyncFinished.value = true

        assertEquals(Locale.ENGLISH, dataModel.activeLocale.value)
        isConnnected.value = false
        assertEquals(Locale.FRENCH, dataModel.activeLocale.value)
    }

    @Test
    fun `Property activeLocale - Persisted via SavedStateHandle`() {
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH)
        assertEquals(Locale.ENGLISH, dataModel.activeLocale.value)

        // creating a new DataModel using the same SavedState emulates recreation after process death
        val dataModel2 = MultiLanguageToolActivityDataModel(
            mockk(),
            mockk(),
            manifestManager,
            toolsRepository,
            translationsRepository,
            mockk(),
            isConnnected,
            savedStateHandle
        )
        assertEquals(Locale.ENGLISH, dataModel2.activeLocale.value)
    }
    // endregion Property: activeLocale

    // region Property: visibleLocales
    @Test
    fun `Property visibleLocales - First Primary Downloaded`() {
        // setup test
        everyGetTranslation(TOOL, Locale.FRENCH) returns flowOf(randomTranslation())
        everyGetTranslation(TOOL, Locale.GERMAN) returns flowOf()
        everyGetManifest(TOOL, Locale.FRENCH) returns flowOf(Manifest())
        everyGetManifest(TOOL, Locale.GERMAN) returns flowOf()
        dataModel.toolCode.value = TOOL
        dataModel.supportedType.value = null
        dataModel.activeLocale.value = Locale.FRENCH
        dataModel.primaryLocales.value = listOf(Locale.FRENCH, Locale.GERMAN)
        dataModel.visibleLocales.observeForever(mockk(relaxUnitFun = true))

        // run logic and verify results
        assertThat(
            "first language should be visible because it is downloaded",
            dataModel.visibleLocales.value,
            contains(Locale.FRENCH)
        )
    }

    @Test
    fun `Property visibleLocales - First Primary Loading, Active Second Primary Downloaded`() {
        // setup test
        everyGetTranslation(TOOL, Locale.FRENCH) returns flowOf(randomTranslation())
        everyGetTranslation(TOOL, Locale.GERMAN) returns flowOf(randomTranslation())
        everyGetManifest(TOOL, Locale.FRENCH) returns flowOf()
        everyGetManifest(TOOL, Locale.GERMAN) returns flowOf(Manifest())
        dataModel.toolCode.value = TOOL
        dataModel.supportedType.value = null
        dataModel.activeLocale.value = Locale.FRENCH
        dataModel.primaryLocales.value = listOf(Locale.FRENCH, Locale.GERMAN)
        dataModel.visibleLocales.observeForever(mockk(relaxUnitFun = true))

        // run logic and verify results
        assertThat(
            "french is available because it is currently active and potentially available",
            dataModel.availableLocales.value,
            contains(Locale.FRENCH)
        )
        assertThat(
            "neither language should be visible because the preferred primary is still loading",
            dataModel.visibleLocales.value,
            empty()
        )
    }

    @Test
    fun `Property visibleLocales - First Primary Missing, Second Primary Loading, Neither Active`() {
        // setup test
        everyGetTranslation(TOOL, Locale.FRENCH) returns flowOf(null)
        everyGetTranslation(TOOL, Locale.GERMAN) returns flowOf(randomTranslation())
        everyGetManifest(TOOL, Locale.FRENCH) returns flowOf(null)
        everyGetManifest(TOOL, Locale.GERMAN) returns flowOf()
        dataModel.toolCode.value = TOOL
        dataModel.supportedType.value = null
        dataModel.activeLocale.value = Locale.ENGLISH
        dataModel.primaryLocales.value = listOf(Locale.FRENCH, Locale.GERMAN)
        dataModel.isInitialSyncFinished.value = true
        dataModel.visibleLocales.observeForever(mockk(relaxUnitFun = true))

        // run logic and verify results
        assertThat(
            "german should be available because it is potentially available",
            dataModel.availableLocales.value,
            contains(Locale.GERMAN)
        )
        assertThat(
            "neither language should be visible because the primary is still loading",
            dataModel.visibleLocales.value,
            empty()
        )
    }

    @Test
    fun `Property visibleLocales - First Primary Loaded, Second Primary Loading And Active`() {
        // setup test
        everyGetTranslation(TOOL, Locale.FRENCH) returns flowOf(randomTranslation())
        everyGetTranslation(TOOL, Locale.GERMAN) returns flowOf(randomTranslation())
        everyGetManifest(TOOL, Locale.FRENCH) returns flowOf(Manifest())
        everyGetManifest(TOOL, Locale.GERMAN) returns flowOf()
        dataModel.toolCode.value = TOOL
        dataModel.supportedType.value = null
        dataModel.activeLocale.value = Locale.GERMAN
        dataModel.primaryLocales.value = listOf(Locale.FRENCH, Locale.GERMAN)
        dataModel.visibleLocales.observeForever(mockk(relaxUnitFun = true))

        // run logic and verify results
        assertThat(
            "german should be available because it is potentially available",
            dataModel.availableLocales.value,
            contains(Locale.GERMAN)
        )
        assertThat(
            "neither language should be visible because the primary is still loading",
            dataModel.visibleLocales.value,
            empty()
        )
    }
    // endregion Property: visibleLocales
}
