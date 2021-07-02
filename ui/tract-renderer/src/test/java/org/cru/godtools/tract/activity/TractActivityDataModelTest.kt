package org.cru.godtools.tract.activity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.nullableArgumentCaptor
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import java.util.Locale
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.cru.godtools.base.tool.activity.BaseToolActivity.LoadingState
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Translation
import org.cru.godtools.tool.model.Manifest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.aMapWithSize
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anEmptyMap
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.hasEntry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.db.GodToolsDao

private const val TOOL = "kgp"

@RunWith(AndroidJUnit4::class)
class TractActivityDataModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var dao: GodToolsDao
    private lateinit var downloadManager: GodToolsDownloadManager
    private lateinit var manifestManager: ManifestManager
    private lateinit var dataModel: TractActivityDataModel

    private val isConnnected = MutableLiveData(true)
    private lateinit var observer: Observer<Any?>

    @Before
    fun setupDataModel() {
        dao = mock()
        downloadManager = mock()
        manifestManager = mock()
        dataModel = TractActivityDataModel(dao, downloadManager, manifestManager, isConnnected, SavedStateHandle())
            .apply { isInitialSyncFinished.value = true }
    }

    @Before
    fun setupObserver() {
        observer = mock()
    }

    // region Property: activeManifest
    @Test
    fun verifyActiveManifestChangeActiveLocale() {
        wheneverGetManifest(TOOL, Locale.ENGLISH).thenReturn(emptyLiveData())
        wheneverGetManifest(TOOL, Locale.FRENCH).thenReturn(MutableLiveData())
        dataModel.tool.value = TOOL
        dataModel.setActiveLocale(Locale.ENGLISH)

        dataModel.activeManifest.observeForever(observer)
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.ENGLISH))
        verify(manifestManager, never()).getLatestPublishedManifestLiveData(any(), eq(Locale.FRENCH))
        dataModel.setActiveLocale(Locale.FRENCH)
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.ENGLISH))
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.FRENCH))
        nullableArgumentCaptor<Manifest> {
            verify(observer, times(2)).onChanged(capture())
        }
    }
    // endregion Property: activeManifest

    // region Property: manifests
    @Test
    fun verifyManifests() {
        wheneverGetManifest(TOOL, Locale.ENGLISH).thenReturn(emptyLiveData())
        wheneverGetManifest(TOOL, Locale.FRENCH).thenReturn(MutableLiveData())
        dataModel.tool.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)

        dataModel.manifests.observeForever(observer)
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.ENGLISH))
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.FRENCH))
        argumentCaptor<Map<Locale, Manifest?>> {
            verify(observer).onChanged(capture())
            assertThat(
                lastValue,
                allOf(
                    aMapWithSize(2),
                    hasEntry<Locale, Manifest?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Manifest?>(Locale.FRENCH, null)
                )
            )
        }
    }

    @Test
    fun verifyManifestsNoLocales() {
        dataModel.manifests.observeForever(observer)
        assertThat(dataModel.manifests.value, anEmptyMap())
        dataModel.tool.value = TOOL
        assertThat(dataModel.manifests.value, anEmptyMap())
        verify(observer).onChanged(eq(emptyMap<Locale, Manifest?>()))
    }

    @Test
    fun verifyManifestsUpdateLocales() {
        val french = MutableLiveData(Manifest())
        wheneverGetManifest(TOOL, Locale.ENGLISH).thenReturn(emptyLiveData())
        wheneverGetManifest(TOOL, Locale.FRENCH).thenReturn(french)
        dataModel.tool.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)

        dataModel.manifests.observeForever(observer)
        dataModel.primaryLocales.value = listOf(Locale.FRENCH)
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.ENGLISH))
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.FRENCH))
        argumentCaptor<Map<Locale, Manifest?>> {
            verify(observer, times(2)).onChanged(capture())
            assertThat(
                firstValue,
                allOf(
                    aMapWithSize(2),
                    hasEntry<Locale, Manifest?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Manifest?>(Locale.FRENCH, french.value)
                )
            )
            assertThat(lastValue, allOf(aMapWithSize(1), hasEntry<Locale, Manifest?>(Locale.FRENCH, french.value)))
        }
    }

    @Test
    fun verifyManifestsUpdateManifest() {
        val french = MutableLiveData<Manifest?>()
        wheneverGetManifest(TOOL, Locale.ENGLISH).thenReturn(emptyLiveData())
        wheneverGetManifest(TOOL, Locale.FRENCH).thenReturn(french)
        dataModel.tool.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)
        dataModel.manifests.observeForever(observer)
        french.value = Manifest()

        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.ENGLISH))
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.FRENCH))
        argumentCaptor<Map<Locale, Manifest?>> {
            verify(observer, times(2)).onChanged(capture())
            assertThat(
                firstValue,
                allOf(
                    aMapWithSize(2),
                    hasEntry<Locale, Manifest?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Manifest?>(Locale.FRENCH, null)
                )
            )
            assertThat(
                lastValue,
                allOf(
                    aMapWithSize(2),
                    hasEntry<Locale, Manifest?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Manifest?>(Locale.FRENCH, french.value)
                )
            )
        }
    }
    // endregion Property: manifests

    // region Property: translations
    @Test
    fun verifyTranslations() {
        val translation = Translation()
        wheneverGetTranslation(TOOL, Locale.ENGLISH).thenReturn(emptyLiveData())
        wheneverGetTranslation(TOOL, Locale.FRENCH).thenReturn(MutableLiveData())
        wheneverGetTranslation(TOOL, Locale.CHINESE).thenReturn(MutableLiveData(translation))
        dataModel.tool.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH, Locale.CHINESE)

        dataModel.translations.observeForever(observer)
        verify(dao).getLatestTranslationLiveData(any(), eq(Locale.ENGLISH), any(), any(), any())
        verify(dao).getLatestTranslationLiveData(any(), eq(Locale.FRENCH), any(), any(), any())
        verify(dao).getLatestTranslationLiveData(any(), eq(Locale.CHINESE), any(), any(), any())
        argumentCaptor<Map<Locale, Translation?>> {
            verify(observer).onChanged(capture())
            assertThat(
                lastValue,
                allOf(
                    aMapWithSize(3),
                    hasEntry<Locale, Translation?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Translation?>(Locale.FRENCH, null),
                    hasEntry(Locale.CHINESE, translation)
                )
            )
        }
    }

    @Test
    fun verifyTranslationsNoLocales() {
        dataModel.translations.observeForever(observer)
        assertThat(dataModel.translations.value, anEmptyMap())
        dataModel.tool.value = TOOL
        assertThat(dataModel.translations.value, anEmptyMap())
        verify(observer).onChanged(eq(emptyMap<Locale, Translation?>()))
    }

    @Test
    fun verifyTranslationsUpdateTranslation() {
        val french = MutableLiveData<Translation?>(null)
        wheneverGetTranslation(TOOL, Locale.ENGLISH).thenReturn(emptyLiveData())
        wheneverGetTranslation(TOOL, Locale.FRENCH).thenReturn(french)
        dataModel.tool.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)

        dataModel.translations.observeForever(observer)
        french.value = Translation()
        verify(dao).getLatestTranslationLiveData(any(), eq(Locale.ENGLISH), any(), any(), any())
        verify(dao).getLatestTranslationLiveData(any(), eq(Locale.FRENCH), any(), any(), any())
        argumentCaptor<Map<Locale, Translation?>> {
            verify(observer, times(2)).onChanged(capture())
            assertThat(
                firstValue,
                allOf(
                    aMapWithSize(2),
                    hasEntry<Locale, Translation?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Translation?>(Locale.FRENCH, null)
                )
            )
            assertThat(
                lastValue,
                allOf(
                    aMapWithSize(2),
                    hasEntry<Locale, Translation?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Translation?>(Locale.FRENCH, french.value)
                )
            )
        }
    }
    // endregion Property: translations

    // region Property: loadingState
    @Test
    fun verifyLoadingStateUpdateTranslation() {
        val translation = MutableLiveData<Translation?>(null)
        dataModel.tool.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH)
        wheneverGetManifest(any(), any()).thenReturn(emptyLiveData())
        wheneverGetTranslation(TOOL, Locale.ENGLISH).thenReturn(translation)

        dataModel.loadingState.observeForever(observer)
        assertThat(
            dataModel.loadingState.value,
            allOf(aMapWithSize(1), hasEntry(Locale.ENGLISH, LoadingState.NOT_FOUND))
        )
        translation.value = Translation()
        assertThat(dataModel.loadingState.value, allOf(aMapWithSize(1), hasEntry(Locale.ENGLISH, LoadingState.LOADING)))
        argumentCaptor<Map<Locale, LoadingState>> {
            verify(observer, times(2)).onChanged(capture())
            assertThat(firstValue, allOf(aMapWithSize(1), hasEntry(Locale.ENGLISH, LoadingState.NOT_FOUND)))
            assertThat(lastValue, allOf(aMapWithSize(1), hasEntry(Locale.ENGLISH, LoadingState.LOADING)))
        }
    }
    // endregion Property: loadingState

    // region Property: visibleLocales
    @Test
    fun verifyVisibleLocalesFirstPrimaryDownloaded() {
        // setup test
        wheneverGetTranslation(TOOL, Locale.FRENCH).thenReturn(MutableLiveData(Translation()))
        wheneverGetTranslation(TOOL, Locale.GERMAN).thenReturn(MutableLiveData())
        wheneverGetManifest(TOOL, Locale.FRENCH).thenReturn(MutableLiveData(Manifest()))
        wheneverGetManifest(TOOL, Locale.GERMAN).thenReturn(MutableLiveData())
        dataModel.tool.value = TOOL
        dataModel.setActiveLocale(Locale.FRENCH)
        dataModel.primaryLocales.value = listOf(Locale.FRENCH, Locale.GERMAN)
        dataModel.visibleLocales.observeForever(observer)

        // run logic and verify results
        assertThat(
            "first language should be visible because it is downloaded",
            dataModel.visibleLocales.value, contains(Locale.FRENCH)
        )
    }

    @Test
    fun verifyVisibleLocalesFirstPrimaryLoadingAndActiveSecondPrimaryDownloaded() {
        // setup test
        wheneverGetTranslation(TOOL, Locale.FRENCH).thenReturn(MutableLiveData(Translation()))
        wheneverGetTranslation(TOOL, Locale.GERMAN).thenReturn(MutableLiveData(Translation()))
        wheneverGetManifest(TOOL, Locale.FRENCH).thenReturn(MutableLiveData())
        wheneverGetManifest(TOOL, Locale.GERMAN).thenReturn(MutableLiveData(Manifest()))
        dataModel.tool.value = TOOL
        dataModel.setActiveLocale(Locale.FRENCH)
        dataModel.primaryLocales.value = listOf(Locale.FRENCH, Locale.GERMAN)
        dataModel.visibleLocales.observeForever(observer)

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
    fun verifyVisibleLocalesFirstPrimaryMissingSecondPrimaryLoadingNeitherActive() {
        // setup test
        wheneverGetTranslation(TOOL, Locale.FRENCH).thenReturn(MutableLiveData(null))
        wheneverGetTranslation(TOOL, Locale.GERMAN).thenReturn(MutableLiveData(Translation()))
        wheneverGetManifest(TOOL, Locale.FRENCH).thenReturn(MutableLiveData(null))
        wheneverGetManifest(TOOL, Locale.GERMAN).thenReturn(MutableLiveData())
        dataModel.tool.value = TOOL
        dataModel.setActiveLocale(Locale.ENGLISH)
        dataModel.primaryLocales.value = listOf(Locale.FRENCH, Locale.GERMAN)
        dataModel.visibleLocales.observeForever(observer)

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
    fun verifyVisibleLocalesFirstPrimaryLoadedSecondPrimaryLoadingAndActive() {
        // setup test
        wheneverGetTranslation(TOOL, Locale.FRENCH).thenReturn(MutableLiveData(Translation()))
        wheneverGetTranslation(TOOL, Locale.GERMAN).thenReturn(MutableLiveData(Translation()))
        wheneverGetManifest(TOOL, Locale.FRENCH).thenReturn(MutableLiveData(Manifest()))
        wheneverGetManifest(TOOL, Locale.GERMAN).thenReturn(MutableLiveData())
        dataModel.tool.value = TOOL
        dataModel.setActiveLocale(Locale.GERMAN)
        dataModel.primaryLocales.value = listOf(Locale.FRENCH, Locale.GERMAN)
        dataModel.visibleLocales.observeForever(observer)

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

    private fun wheneverGetManifest(tool: String, locale: Locale) =
        whenever(manifestManager.getLatestPublishedManifestLiveData(tool, locale))

    private fun wheneverGetTranslation(tool: String, locale: Locale) =
        whenever(dao.getLatestTranslationLiveData(tool, locale, trackAccess = true))
}
