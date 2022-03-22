package org.cru.godtools.base.tool.activity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.verifyAll
import io.mockk.verifySequence
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.db.findAsFlow
import org.cru.godtools.base.tool.activity.BaseToolActivity.LoadingState
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.tool.model.Manifest
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
import org.keynote.godtools.android.db.GodToolsDao

private const val TOOL = "kgp"

@OptIn(ExperimentalCoroutinesApi::class)
class MultiLanguageToolActivityDataModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // region Objects & Mocks
    private lateinit var dao: GodToolsDao
    private lateinit var manifestManager: ManifestManager
    private lateinit var dataModel: MultiLanguageToolActivityDataModel
    private val isConnnected = MutableLiveData(true)

    @Before
    fun setupDataModel() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        dao = mockk {
            every { findAsFlow<Tool>(any<String>()) } returns emptyFlow()
            every { getLatestTranslationLiveData(any(), any(), trackAccess = true) } returns MutableLiveData()
        }
        manifestManager = mockk {
            every { getLatestPublishedManifestLiveData(any(), any()) } returns MutableLiveData()
        }
        dataModel = MultiLanguageToolActivityDataModel(
            dao,
            mockk(),
            manifestManager,
            isConnnected,
            SavedStateHandle()
        )
        excludeRecords { dao.findAsFlow<Tool>(any<String>()) }
    }

    @After
    fun cleanupDataModel() {
        Dispatchers.resetMain()
    }

    private fun everyGetManifest(tool: String, locale: Locale) =
        every { manifestManager.getLatestPublishedManifestLiveData(tool, locale) }
    private fun everyGetTranslation(tool: String, locale: Locale) =
        every { dao.getLatestTranslationLiveData(tool, locale, trackAccess = true) }
    // endregion Objects & Mocks

    // region Resolved Data
    // region Property: translations
    @Test
    fun `Property translations`() {
        val translation = Translation()
        everyGetTranslation(TOOL, Locale.ENGLISH) returns emptyLiveData()
        everyGetTranslation(TOOL, Locale.FRENCH) returns MutableLiveData()
        everyGetTranslation(TOOL, Locale.CHINESE) returns MutableLiveData(translation)
        val observer = mockk<Observer<Map<Locale, Translation?>>>(relaxUnitFun = true)
        dataModel.toolCode.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH, Locale.CHINESE)

        dataModel.translations.observeForever(observer)
        verifyAll {
            dao.getLatestTranslationLiveData(TOOL, Locale.ENGLISH, trackAccess = true)
            dao.getLatestTranslationLiveData(TOOL, Locale.FRENCH, trackAccess = true)
            dao.getLatestTranslationLiveData(TOOL, Locale.CHINESE, trackAccess = true)
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
        val french = MutableLiveData<Translation?>(null)
        val translations = mutableListOf<Map<Locale, Translation?>>()
        val observer = mockk<Observer<Map<Locale, Translation?>>> {
            every { onChanged(capture(translations)) } returns Unit
        }
        everyGetTranslation(TOOL, Locale.ENGLISH) returns emptyLiveData()
        everyGetTranslation(TOOL, Locale.FRENCH) returns french
        dataModel.toolCode.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)

        dataModel.translations.observeForever(observer)
        french.value = Translation()
        verifyAll {
            dao.getLatestTranslationLiveData(TOOL, Locale.ENGLISH, trackAccess = true)
            dao.getLatestTranslationLiveData(TOOL, Locale.FRENCH, trackAccess = true)
            repeat(2) { observer.onChanged(any()) }
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
        everyGetManifest(TOOL, Locale.ENGLISH) returns emptyLiveData()
        everyGetManifest(TOOL, Locale.FRENCH) returns MutableLiveData()
        dataModel.toolCode.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)

        dataModel.manifests.observeForever(observer)
        verifyAll {
            manifestManager.getLatestPublishedManifestLiveData(any(), Locale.ENGLISH)
            manifestManager.getLatestPublishedManifestLiveData(any(), Locale.FRENCH)
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
    fun `Property manifests - Update Locales`() {
        val french = MutableLiveData(Manifest())
        val manifests = mutableListOf<Map<Locale, Manifest?>>()
        val observer = mockk<Observer<Map<Locale, Manifest?>>> {
            every { onChanged(capture(manifests)) } returns Unit
        }
        everyGetManifest(TOOL, Locale.ENGLISH) returns emptyLiveData()
        everyGetManifest(TOOL, Locale.FRENCH) returns french
        dataModel.toolCode.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)

        dataModel.manifests.observeForever(observer)
        dataModel.primaryLocales.value = listOf(Locale.FRENCH)
        verifyAll {
            manifestManager.getLatestPublishedManifestLiveData(any(), Locale.ENGLISH)
            manifestManager.getLatestPublishedManifestLiveData(any(), Locale.FRENCH)
            repeat(2) { observer.onChanged(any()) }
        }
        assertThat(
            manifests,
            contains(
                allOf(
                    aMapWithSize(2),
                    hasEntry<Locale, Manifest?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Manifest?>(Locale.FRENCH, french.value)
                ),
                allOf(aMapWithSize(1), hasEntry<Locale, Manifest?>(Locale.FRENCH, french.value))
            )
        )
    }

    @Test
    fun `Property manifests - Update Manifest`() {
        val french = MutableLiveData<Manifest?>()
        val manifests = mutableListOf<Map<Locale, Manifest?>>()
        val observer = mockk<Observer<Map<Locale, Manifest?>>> {
            every { onChanged(capture(manifests)) } returns Unit
        }
        everyGetManifest(TOOL, Locale.ENGLISH) returns emptyLiveData()
        everyGetManifest(TOOL, Locale.FRENCH) returns french
        dataModel.toolCode.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)
        dataModel.manifests.observeForever(observer)

        french.value = Manifest()
        verifyAll {
            manifestManager.getLatestPublishedManifestLiveData(any(), Locale.ENGLISH)
            manifestManager.getLatestPublishedManifestLiveData(any(), Locale.FRENCH)
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
        val translation = MutableLiveData<Translation?>(null)
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
        translation.value = Translation()
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

    // region Active Tool
    // region Property: activeLocale
    @Test
    fun `Property activeLocale - Initialize when locales initialized`() {
        assertNull(dataModel.activeLocale.value)
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH)
        assertEquals(Locale.ENGLISH, dataModel.activeLocale.value)
    }

    @Test
    fun `Property activeLocale - Updates when activeLoadingState is INVALID_TYPE`() {
        val englishManifest = MutableLiveData<Manifest?>()
        everyGetTranslation(TOOL, Locale.ENGLISH) returns MutableLiveData(Translation())
        everyGetTranslation(TOOL, Locale.FRENCH) returns MutableLiveData(Translation())
        everyGetManifest(TOOL, Locale.ENGLISH) returns englishManifest
        everyGetManifest(TOOL, Locale.FRENCH) returns MutableLiveData(Manifest(type = Manifest.Type.TRACT))
        dataModel.toolCode.value = TOOL
        dataModel.supportedType.value = Manifest.Type.TRACT
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)
        dataModel.isInitialSyncFinished.value = true

        assertEquals(Locale.ENGLISH, dataModel.activeLocale.value)
        englishManifest.value = Manifest(type = Manifest.Type.LESSON)
        assertEquals(Locale.FRENCH, dataModel.activeLocale.value)
    }

    @Test
    fun `Property activeLocale - Updates when activeLoadingState is NOT_FOUND`() {
        val englishTranslation = MutableLiveData<Translation?>()
        everyGetTranslation(TOOL, Locale.ENGLISH) returns englishTranslation
        everyGetTranslation(TOOL, Locale.FRENCH) returns MutableLiveData(Translation())
        everyGetManifest(TOOL, Locale.ENGLISH) returns emptyLiveData()
        everyGetManifest(TOOL, Locale.FRENCH) returns MutableLiveData(Manifest(type = Manifest.Type.TRACT))
        dataModel.toolCode.value = TOOL
        dataModel.supportedType.value = Manifest.Type.TRACT
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)
        dataModel.isInitialSyncFinished.value = true

        assertEquals(Locale.ENGLISH, dataModel.activeLocale.value)
        englishTranslation.value = null
        assertEquals(Locale.FRENCH, dataModel.activeLocale.value)
    }

    @Test
    fun `Property activeLocale - Updates when activeLoadingState is OFFLINE`() {
        everyGetTranslation(TOOL, Locale.ENGLISH) returns MutableLiveData(Translation())
        everyGetTranslation(TOOL, Locale.FRENCH) returns MutableLiveData(Translation())
        everyGetManifest(TOOL, Locale.ENGLISH) returns emptyLiveData()
        everyGetManifest(TOOL, Locale.FRENCH) returns MutableLiveData(Manifest(type = Manifest.Type.TRACT))
        dataModel.toolCode.value = TOOL
        dataModel.supportedType.value = Manifest.Type.TRACT
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)
        dataModel.isInitialSyncFinished.value = true

        assertEquals(Locale.ENGLISH, dataModel.activeLocale.value)
        isConnnected.value = false
        assertEquals(Locale.FRENCH, dataModel.activeLocale.value)
    }
    // endregion Property: activeLocale

    // region Property: activeManifest
    @Test
    fun `Property activeManifest - Change Active Locale`() {
        val frenchManifest = Manifest(type = Manifest.Type.TRACT)
        val observer = mockk<Observer<Manifest?>>(relaxUnitFun = true)
        everyGetManifest(TOOL, Locale.ENGLISH) returns emptyLiveData()
        everyGetManifest(TOOL, Locale.FRENCH) returns MutableLiveData(frenchManifest)
        dataModel.toolCode.value = TOOL
        dataModel.supportedType.value = Manifest.Type.TRACT
        dataModel.activeLocale.value = Locale.ENGLISH

        dataModel.activeManifest.observeForever(observer)
        verifySequence {
            manifestManager.getLatestPublishedManifestLiveData(any(), Locale.ENGLISH)
            observer.onChanged(null)
        }
        dataModel.activeLocale.value = Locale.FRENCH
        verifySequence {
            manifestManager.getLatestPublishedManifestLiveData(any(), Locale.ENGLISH)
            observer.onChanged(null)
            manifestManager.getLatestPublishedManifestLiveData(any(), Locale.FRENCH)
            observer.onChanged(frenchManifest)
        }
    }
    // endregion Property: activeManifest
    // endregion Active Tool

    // region Property: visibleLocales
    @Test
    fun `Property visibleLocales - First Primary Downloaded`() {
        // setup test
        everyGetTranslation(TOOL, Locale.FRENCH) returns MutableLiveData(Translation())
        everyGetTranslation(TOOL, Locale.GERMAN) returns MutableLiveData()
        everyGetManifest(TOOL, Locale.FRENCH) returns MutableLiveData(Manifest())
        everyGetManifest(TOOL, Locale.GERMAN) returns MutableLiveData()
        dataModel.toolCode.value = TOOL
        dataModel.supportedType.value = null
        dataModel.activeLocale.value = Locale.FRENCH
        dataModel.primaryLocales.value = listOf(Locale.FRENCH, Locale.GERMAN)
        dataModel.visibleLocales.observeForever(mockk(relaxUnitFun = true))

        // run logic and verify results
        assertThat(
            "first language should be visible because it is downloaded",
            dataModel.visibleLocales.value, contains(Locale.FRENCH)
        )
    }

    @Test
    fun `Property visibleLocales - First Primary Loading, Active Second Primary Downloaded`() {
        // setup test
        everyGetTranslation(TOOL, Locale.FRENCH) returns MutableLiveData(Translation())
        everyGetTranslation(TOOL, Locale.GERMAN) returns MutableLiveData(Translation())
        everyGetManifest(TOOL, Locale.FRENCH) returns MutableLiveData()
        everyGetManifest(TOOL, Locale.GERMAN) returns MutableLiveData(Manifest())
        dataModel.toolCode.value = TOOL
        dataModel.supportedType.value = null
        dataModel.activeLocale.value = Locale.FRENCH
        dataModel.primaryLocales.value = listOf(Locale.FRENCH, Locale.GERMAN)
        dataModel.visibleLocales.observeForever(mockk(relaxUnitFun = true))

        // run logic and verify results
        assertThat(
            "french is available because it is currently active and potentially available",
            dataModel.availableLocales.value, contains(Locale.FRENCH)
        )
        assertThat(
            "neither language should be visible because the preferred primary is still loading",
            dataModel.visibleLocales.value, empty()
        )
    }

    @Test
    fun `Property visibleLocales - First Primary Missing, Second Primary Loading, Neither Active`() {
        // setup test
        everyGetTranslation(TOOL, Locale.FRENCH) returns MutableLiveData(null)
        everyGetTranslation(TOOL, Locale.GERMAN) returns MutableLiveData(Translation())
        everyGetManifest(TOOL, Locale.FRENCH) returns MutableLiveData(null)
        everyGetManifest(TOOL, Locale.GERMAN) returns MutableLiveData()
        dataModel.toolCode.value = TOOL
        dataModel.supportedType.value = null
        dataModel.activeLocale.value = Locale.ENGLISH
        dataModel.primaryLocales.value = listOf(Locale.FRENCH, Locale.GERMAN)
        dataModel.isInitialSyncFinished.value = true
        dataModel.visibleLocales.observeForever(mockk(relaxUnitFun = true))

        // run logic and verify results
        assertThat(
            "german should be available because it is potentially available",
            dataModel.availableLocales.value, contains(Locale.GERMAN)
        )
        assertThat(
            "neither language should be visible because the primary is still loading",
            dataModel.visibleLocales.value, empty()
        )
    }

    @Test
    fun `Property visibleLocales - First Primary Loaded, Second Primary Loading And Active`() {
        // setup test
        everyGetTranslation(TOOL, Locale.FRENCH) returns MutableLiveData(Translation())
        everyGetTranslation(TOOL, Locale.GERMAN) returns MutableLiveData(Translation())
        everyGetManifest(TOOL, Locale.FRENCH) returns MutableLiveData(Manifest())
        everyGetManifest(TOOL, Locale.GERMAN) returns MutableLiveData()
        dataModel.toolCode.value = TOOL
        dataModel.supportedType.value = null
        dataModel.activeLocale.value = Locale.GERMAN
        dataModel.primaryLocales.value = listOf(Locale.FRENCH, Locale.GERMAN)
        dataModel.visibleLocales.observeForever(mockk(relaxUnitFun = true))

        // run logic and verify results
        assertThat(
            "german should be available because it is potentially available",
            dataModel.availableLocales.value, contains(Locale.GERMAN)
        )
        assertThat(
            "neither language should be visible because the primary is still loading",
            dataModel.visibleLocales.value, empty()
        )
    }
    // endregion Property: visibleLocales
}
