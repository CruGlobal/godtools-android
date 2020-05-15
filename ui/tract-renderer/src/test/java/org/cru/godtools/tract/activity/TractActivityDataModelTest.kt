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
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.cru.godtools.base.tool.activity.BaseToolActivity.Companion.STATE_INVALID_TYPE
import org.cru.godtools.base.tool.activity.BaseToolActivity.Companion.STATE_LOADED
import org.cru.godtools.base.tool.activity.BaseToolActivity.Companion.STATE_LOADING
import org.cru.godtools.base.tool.activity.BaseToolActivity.Companion.STATE_NOT_FOUND
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Translation
import org.cru.godtools.xml.model.Manifest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.empty
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.db.GodToolsDao
import java.util.Locale

private const val TOOL = "kgp"

@RunWith(AndroidJUnit4::class)
class TractActivityDataModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var dao: GodToolsDao
    private lateinit var downloadManager: GodToolsDownloadManager
    private lateinit var manifestManager: ManifestManager
    private lateinit var dataModel: TractActivityDataModel

    private lateinit var observer: Observer<Any?>

    @Before
    fun setupDataModel() {
        dao = mock()
        downloadManager = mock()
        manifestManager = mock()
        dataModel = TractActivityDataModel(dao, downloadManager, manifestManager, SavedStateHandle())
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
        dataModel.activeLocale = Locale.ENGLISH

        dataModel.activeManifest.observeForever(observer)
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.ENGLISH))
        verify(manifestManager, never()).getLatestPublishedManifestLiveData(any(), eq(Locale.FRENCH))
        dataModel.activeLocale = Locale.FRENCH
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
        dataModel.locales.value = listOf(Locale.ENGLISH, Locale.FRENCH)

        dataModel.manifests.observeForever(observer)
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.ENGLISH))
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.FRENCH))
        argumentCaptor<List<Manifest?>> {
            verify(observer).onChanged(capture())
            assertThat(lastValue, contains(null, null))
        }
    }

    @Test
    fun verifyManifestsNoLocales() {
        dataModel.manifests.observeForever(observer)
        assertThat(dataModel.manifests.value, empty())
        dataModel.tool.value = TOOL
        assertThat(dataModel.manifests.value, empty())
        verify(observer).onChanged(eq(emptyList<Manifest?>()))
    }

    @Test
    fun verifyManifestsUpdateLocales() {
        val french = MutableLiveData(Manifest())
        wheneverGetManifest(TOOL, Locale.ENGLISH).thenReturn(emptyLiveData())
        wheneverGetManifest(TOOL, Locale.FRENCH).thenReturn(french)
        dataModel.tool.value = TOOL
        dataModel.locales.value = listOf(Locale.ENGLISH, Locale.FRENCH)

        dataModel.manifests.observeForever(observer)
        dataModel.locales.value = listOf(Locale.FRENCH)
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.ENGLISH))
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.FRENCH))
        argumentCaptor<List<Manifest?>> {
            verify(observer, times(2)).onChanged(capture())
            assertThat(firstValue, contains(null, french.value))
            assertThat(lastValue, contains(french.value))
        }
    }

    @Test
    fun verifyManifestsUpdateManifest() {
        val french = MutableLiveData<Manifest?>()
        wheneverGetManifest(TOOL, Locale.ENGLISH).thenReturn(emptyLiveData())
        wheneverGetManifest(TOOL, Locale.FRENCH).thenReturn(french)
        dataModel.tool.value = TOOL
        dataModel.locales.value = listOf(Locale.ENGLISH, Locale.FRENCH)
        dataModel.manifests.observeForever(observer)
        french.value = Manifest()

        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.ENGLISH))
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.FRENCH))
        argumentCaptor<List<Manifest?>> {
            verify(observer, times(2)).onChanged(capture())
            assertThat(firstValue, contains(null, null))
            assertThat(lastValue, contains(null, french.value))
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
        dataModel.locales.value = listOf(Locale.ENGLISH, Locale.FRENCH, Locale.CHINESE)

        dataModel.translations.observeForever(observer)
        verify(dao).getLatestTranslationLiveData(any(), eq(Locale.ENGLISH), any(), any(), any())
        verify(dao).getLatestTranslationLiveData(any(), eq(Locale.FRENCH), any(), any(), any())
        verify(dao).getLatestTranslationLiveData(any(), eq(Locale.CHINESE), any(), any(), any())
        argumentCaptor<List<Translation?>> {
            verify(observer).onChanged(capture())
            assertThat(lastValue, contains(null, null, translation))
        }
    }

    @Test
    fun verifyTranslationsNoLocales() {
        dataModel.translations.observeForever(observer)
        assertThat(dataModel.translations.value, empty())
        dataModel.tool.value = TOOL
        assertThat(dataModel.translations.value, empty())
        verify(observer).onChanged(eq(emptyList<Translation?>()))
    }

    @Test
    fun verifyTranslationsUpdateTranslation() {
        val french = MutableLiveData<Translation?>(null)
        wheneverGetTranslation(TOOL, Locale.ENGLISH).thenReturn(emptyLiveData())
        wheneverGetTranslation(TOOL, Locale.FRENCH).thenReturn(french)
        dataModel.tool.value = TOOL
        dataModel.locales.value = listOf(Locale.ENGLISH, Locale.FRENCH)

        dataModel.translations.observeForever(observer)
        french.value = Translation()
        verify(dao).getLatestTranslationLiveData(any(), eq(Locale.ENGLISH), any(), any(), any())
        verify(dao).getLatestTranslationLiveData(any(), eq(Locale.FRENCH), any(), any(), any())
        argumentCaptor<List<Translation?>> {
            verify(observer, times(2)).onChanged(capture())
            assertThat(firstValue, contains(null, null))
            assertThat(lastValue, contains(null, french.value))
        }
    }
    // endregion Property: translations

    @Test
    fun verifyDetermineState() {
        assertEquals(STATE_LOADED, determineState(Manifest().apply { mType = Manifest.Type.TRACT }, null, null))
        assertEquals(STATE_INVALID_TYPE, determineState(Manifest().apply { mType = Manifest.Type.ARTICLE }, null, null))
        assertEquals(STATE_NOT_FOUND, determineState(null, null, false))
        assertEquals(STATE_LOADING, determineState(null, Translation(), null))
        assertEquals(STATE_LOADING, determineState(null, null, true))
    }

    private fun wheneverGetManifest(tool: String, locale: Locale) =
        whenever(manifestManager.getLatestPublishedManifestLiveData(tool, locale))

    private fun wheneverGetTranslation(tool: String, locale: Locale) =
        whenever(dao.getLatestTranslationLiveData(tool, locale, trackAccess = true))
}
